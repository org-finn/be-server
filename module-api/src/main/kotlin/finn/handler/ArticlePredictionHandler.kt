package finn.handler

import finn.converter.SentimentConverter
import finn.exception.NotSupportedTypeException
import finn.queryDto.PredictionUpdateDto
import finn.service.PredictionCommandService
import finn.service.PredictionQueryService
import finn.strategy.ArticleSentimentScoreStrategy
import finn.strategy.StrategyFactory
import finn.task.ArticlePredictionTask
import finn.task.PredictionTask
import finn.transaction.ExposedTransactional
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.springframework.stereotype.Component
import java.sql.Connection
import kotlin.math.roundToInt

@Component
class ArticlePredictionHandler(
    private val predictionCommandService: PredictionCommandService,
    private val sentimentConverter: SentimentConverter,
    private val predictionQueryService: PredictionQueryService,
    private val strategyFactory: StrategyFactory
) : PredictionHandler {

    companion object {
        const val ALPHA = 0.1
        private val log = KotlinLogging.logger {}
    }

    override fun supports(type: String): Boolean = type == "article"

    @ExposedTransactional
    override suspend fun handle(tasks: List<PredictionTask>) {
        // 타입 캐스팅 검증
        val articleTasks = tasks.filterIsInstance<ArticlePredictionTask>()
        if (articleTasks.isEmpty()) return

        newSuspendedTransaction(
            context = Dispatchers.IO,
            transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
        ) {
            // 1. Ticker 별로 그룹화
            val tasksByTicker = articleTasks.groupBy { it.tickerId }
            val tickerIds = tasksByTicker.keys.toList()

            // 2. 현재 상태 일괄 조회
            val currentPredictions =
                predictionQueryService.findAllByTickerIdsForPrediction(tickerIds)
            val predictionMap = currentPredictions.associateBy { it.tickerId }

            // 업데이트할 내역을 담을 리스트
            val updates = mutableListOf<PredictionUpdateDto>()

            // 3. 메모리 상에서 변경사항 누적 (Aggregation)
            tasksByTicker.forEach { (tickerId, tasksForTicker) ->
                // 해당 종목의 현재 상태 (없으면 예외 혹은 생성 로직)
                val prediction = predictionMap[tickerId]
                    ?: throw RuntimeException("Prediction data not found for $tickerId")

                // 누적 변수 초기화 (현재 DB 값에서 시작)
                var currentScore = prediction.sentimentScore
                var posCount = 0L
                var negCount = 0L
                var neuCount = 0L
                val predictionDate = prediction.predictionDate

                // 여러 개의 Task 순차 적용
                tasksForTicker.forEach { task ->
                    val payload = task.payload

                    // 전략 찾기
                    val strategy = strategyFactory.findSentimentScoreStrategy(task.type)
                            as? ArticleSentimentScoreStrategy
                        ?: throw NotSupportedTypeException("Invalid strategy")

                    val newCalculatedScore = strategy.calculate(task) // 전략 실행

                    // 상태 누적
                    currentScore =
                        ((newCalculatedScore * ALPHA) + (currentScore * (1 - ALPHA))).roundToInt()
                    posCount += payload.positiveArticleCount
                    negCount += payload.negativeArticleCount
                    neuCount += payload.neutralArticleCount
                }
                val strategy = sentimentConverter.getStrategyFromScore(currentScore)
                val sentiment = sentimentConverter.getSentiment(strategy)

                val updatedPrediction = PredictionUpdateDto(
                    tickerId, posCount, negCount, neuCount, currentScore,
                    sentiment, strategy.strategy, predictionDate
                )
                log.info { "Will update $tickerId prediction: pos_count: $posCount, neg_count: $negCount, neu_count: $neuCount" }
                updates += updatedPrediction
            }

            // 4. 변경된 내역 일괄 업데이트 (Command Service 호출)
            if (updates.isNotEmpty()) {
                predictionCommandService.updatePredictions(updates, ALPHA)
            }
        }

    }
}