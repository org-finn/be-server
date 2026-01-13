package finn.handler

import finn.entity.TickerScore
import finn.exception.NotSupportedTypeException
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
    private val strategyFactory: StrategyFactory
) : PredictionHandler {

    override fun supports(type: String): Boolean = type == "exponent"

    override suspend fun handle(tasks: List<PredictionTask>) {
        newSuspendedTransaction(
            context = Dispatchers.IO,
            transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
        ) {
            tasks.forEach { task ->
                if (task !is ExponentPredictionTask) {
                    throw NotSupportedTypeException("Unsupported prediction task type in Exponent Prediction: ${task.type}")
                }
                // 모든 ticker score 가져옴
                task.payload.previousScores =
                    predictionQueryService.getAllTickerTodaySentimentScore()

                // 30분 전 exponents 가져옴
                val exponents = task.payload.exponents
                val date = task.payload.priceDate
                exponentService.getRecentExponent(exponents, date) // exponents previousValue에 set

                val sentimentScoreStrategy = strategyFactory.findSentimentScoreStrategy(task.type)
                if (sentimentScoreStrategy !is ExponentSentimentScoreStrategy) {
                    throw NotSupportedTypeException("Unsupported sentiment score strategy in Init Prediction: ${sentimentScoreStrategy.javaClass}")
                }

                // 추가로 조정해야하는 점수를 모든 티커 점수에 더하는 식으로 구현
                val adjustmentScore = sentimentScoreStrategy.calculate(task)
                val calculatedScoreList = mutableListOf<TickerScore>()
                task.payload.previousScores.forEach { prevScore ->
                    val newScore = (prevScore.score + adjustmentScore).coerceIn(0, 100)
                    calculatedScoreList.add(TickerScore(prevScore.tickerId, newScore))
                }

                // Prediction update
                val predictionDate = task.payload.predictionDate
                predictionCommandService.updatePredictionByExponent(
                    predictionDate,
                    calculatedScoreList
                )
            }
        }
    }
}