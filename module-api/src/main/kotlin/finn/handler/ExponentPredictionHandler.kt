package finn.handler

import finn.entity.TickerScore
import finn.exception.NotSupportedTypeException
import finn.service.ExponentQueryService
import finn.service.PredictionCommandService
import finn.service.PredictionQueryService
import finn.strategy.ExponentSentimentScoreStrategy
import finn.strategy.StrategyFactory
import finn.task.ExponentPredictionTask
import finn.task.ExponentPredictionUnitTask
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

    override suspend fun handle(task: PredictionTask) {
        newSuspendedTransaction(
            context = Dispatchers.IO,
            transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
        ) {
            if (task !is ExponentPredictionTask) {
                throw NotSupportedTypeException("Unsupported prediction task type in Exponent Prediction: ${task.type}")
            }
            // 모든 ticker score 가져옴
            task.payload.previousScores = predictionQueryService.getAllTickerTodaySentimentScore()

            // 30분 전 exponent 가져옴
            val exponentId = task.payload.exponentId
            val date = task.payload.priceDate
            task.payload.previousValue = exponentService.getRecentExponent(exponentId, date)

            val strategy = strategyFactory.findStrategy(task.type)
            if (strategy !is ExponentSentimentScoreStrategy) {
                throw NotSupportedTypeException("Unsupported prediction strategy in Exponent Prediction: ${strategy.javaClass}")
            }
            
            // ticker 별로 Indiv Task를 생성하여 하나씩 넘김
            val calculatedScoreList = emptyList<TickerScore>()
            task.payload.previousScores.forEach {
                val unitTask = ExponentPredictionUnitTask(
                    task.tickerId,
                    ExponentPredictionUnitTask.ExponentUnitPayload(it.tickerId, it.score)
                )
                val score = strategy.calculate(unitTask)
                calculatedScoreList.plus(TickerScore(it.tickerId, score))
            }


            // Prediction update
            val predictionDate = task.payload.predictionDate
            predictionCommandService.updatePredictionByExponent(predictionDate, calculatedScoreList)
        }
    }
}