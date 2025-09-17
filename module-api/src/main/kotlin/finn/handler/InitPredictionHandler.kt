package finn.handler

import finn.exception.NotSupportedTypeException
import finn.score.strategy.PredictionInitSentimentScoreStrategy
import finn.score.strategy.StrategyFactory
import finn.score.task.InitPredictionTask
import finn.score.task.PredictionTask
import finn.service.PredictionCommandService
import finn.transaction.ExposedTransactional
import org.springframework.stereotype.Component

@Component
class InitPredictionHandler(
    private val predictionService: PredictionCommandService,
    private val strategyFactory: StrategyFactory
) : PredictionHandler {

    override fun supports(type: String): Boolean = type == "init"

    @ExposedTransactional
    override suspend fun handle(task: PredictionTask) {
        if (task !is InitPredictionTask) {
            throw NotSupportedTypeException("Unsupported prediction task type in Init Prediction: ${task.type}")
        }
        val tickerId = task.tickerId
        val tickerCode = task.payload.tickerCode
        val shortCompanyName = task.payload.shortCompanyName
        val predictionDate = task.payload.predictionDate

        val strategy = strategyFactory.findStrategy(task.type)
        if (strategy !is PredictionInitSentimentScoreStrategy) {
            throw NotSupportedTypeException("Unsupported prediction strategy in Init Prediction: ${strategy.javaClass}")
        }
        task.payload.recentScores = predictionService.getRecentSentimentScores(tickerId)
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