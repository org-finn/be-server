package finn.handler

import finn.exception.NotSupportedTypeException
import finn.score.strategy.ArticleSentimentScoreStrategy
import finn.score.strategy.StrategyFactory
import finn.score.task.ArticlePredictionTask
import finn.score.task.PredictionTask
import finn.service.PredictionCommandService
import finn.transaction.SuspendExposedTransactional
import org.springframework.stereotype.Component

@Component
@SuspendExposedTransactional
class ArticlePredictionHandler(
    private val predictionService: PredictionCommandService,
    private val strategyFactory: StrategyFactory
) : PredictionHandler {

    override fun supports(type: String): Boolean = type == "article"

    override suspend fun handle(task: PredictionTask) {
        if (task !is ArticlePredictionTask) {
            throw NotSupportedTypeException("Unsupported prediction task type in Article Prediction: ${task.type}")
        }
        val tickerId = task.tickerId

        val strategy = strategyFactory.findStrategy(task.type)
        if (strategy !is ArticleSentimentScoreStrategy) {
            throw NotSupportedTypeException("Unsupported prediction strategy in Article Prediction: ${strategy.javaClass}")
        }
        task.payload.previousScore = predictionService.getTodaySentimentScore(tickerId)
        val score = strategy.calculate(task)

        predictionService.updatePredictionByArticle(
            tickerId,
            task.payload.predictionDate,
            task.payload.positiveArticleCount,
            task.payload.negativeArticleCount,
            task.payload.neutralArticleCount,
            score
        )
        println("end job in handler")
    }
}