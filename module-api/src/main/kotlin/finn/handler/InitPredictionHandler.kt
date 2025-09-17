package finn.handler

import finn.score.PredictionTask
import finn.score.strategy.StrategyFactory
import finn.service.PredictionCommandService
import finn.transaction.ExposedTransactional
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class InitPredictionHandler(
    private val predictionService: PredictionCommandService,
    private val strategyFactory: StrategyFactory
) : PredictionHandler {

    override fun supports(type: String): Boolean = type == "init"

    @ExposedTransactional
    override suspend fun handle(task: PredictionTask) {
        val tickerId = task.tickerId
        val tickerCode = task.payload["tickerCode"] as String
        val shortCompanyName = task.payload["shortCompanyName"] as String
        val predictionDate = task.payload["predictionDate"] as OffsetDateTime

        val strategy = strategyFactory.findStrategy(task.type)
        task.payload["recentScores"] = predictionService.getRecentSentimentScores(tickerId)
        val score = strategy.calculate(task)

        predictionService.createPrediction(
            tickerId,
            tickerCode,
            shortCompanyName,
            score,
            predictionDate
        )
    }
}