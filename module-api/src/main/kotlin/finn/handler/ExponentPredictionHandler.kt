package finn.handler

import finn.converter.SentimentConverter
import finn.exception.NotSupportedTypeException
import finn.queryDto.PredictionUpdateDto
import finn.service.ExponentQueryService
import finn.service.PredictionCommandService
import finn.service.PredictionQueryService
import finn.strategy.ExponentSentimentScoreStrategy
import finn.strategy.StrategyFactory
import finn.task.ExponentPredictionTask
import finn.task.PredictionTask
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.springframework.stereotype.Component
import java.sql.Connection

@Component
class ExponentPredictionHandler(
    private val predictionCommandService: PredictionCommandService,
    private val predictionQueryService: PredictionQueryService,
    private val exponentService: ExponentQueryService,
    private val strategyFactory: StrategyFactory,
    private val sentimentConverter: SentimentConverter
) : PredictionHandler {

    companion object {
        val ALPHA = 0.1
    }

    override fun supports(type: String): Boolean = type == "exponent"

    override suspend fun handle(tasks: List<PredictionTask>) {
        val exponentTasks = tasks.filterIsInstance<ExponentPredictionTask>()
        if (exponentTasks.isEmpty()) return

        newSuspendedTransaction(
            context = Dispatchers.IO,
            transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
        ) {
            // 1. 배치 내의 모든 Task에 대한 '총 조정 점수'를 먼저 계산
            //    (DB 락을 잡고 있는 시간을 최소화하기 위해 외부 데이터 조회 및 계산을 먼저 수행)
            var totalAdjustment = 0

            exponentTasks.forEach { task ->
                // 외부 데이터 조회 (필요하다면 이 부분도 exponentService 내에서 배치로 최적화 가능)
                exponentService.getRecentExponent(task.payload.exponents, task.payload.priceDate)

                val strategy = strategyFactory.findSentimentScoreStrategy(task.type)
                if (strategy !is ExponentSentimentScoreStrategy) {
                    throw NotSupportedTypeException("Invalid strategy: ${strategy.javaClass}")
                }

                // 각 태스크가 기여하는 조정 점수 합산
                totalAdjustment += strategy.calculate(task)
            }

            if (totalAdjustment == 0) return@newSuspendedTransaction

            // 2. 전체 티커의 현재 점수 조회 (Pessimistic Write Lock)
            //    "모든" 티커를 업데이트하므로 전체 락이 필요합니다.
            val allPredictions = predictionQueryService.findAllForPrediction()

            // 3. 메모리 상에서 점수 조정 및 DTO 생성
            val updates = allPredictions.map { prediction ->
                // 새 점수 계산 (0~100 범위 제한)
                val newScore = (prediction.sentimentScore + totalAdjustment).coerceIn(0, 100)
                val strategyResult = sentimentConverter.getStrategyFromScore(newScore)

                PredictionUpdateDto(
                    tickerId = prediction.tickerId,
                    score = newScore,
                    positiveArticleCount = prediction.positiveArticleCount,
                    negativeArticleCount = prediction.negativeArticleCount,
                    neutralArticleCount = prediction.neutralArticleCount,
                    sentiment = sentimentConverter.getSentiment(strategyResult),
                    strategy = strategyResult.strategy,
                    predictionDate = prediction.predictionDate
                )
            }

            // 4. Bulk Update 수행
            if (updates.isNotEmpty()) {
                predictionCommandService.updatePredictions(updates, ALPHA)
            }
        }
    }
}