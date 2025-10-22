package finn.handler

import finn.exception.NotSupportedTypeException
import finn.service.PredictionCommandService
import finn.service.PredictionQueryService
import finn.strategy.ArticleSentimentScoreStrategy
import finn.strategy.StrategyFactory
import finn.task.ArticlePredictionTask
import finn.task.PredictionTask
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.springframework.stereotype.Component
import java.sql.Connection

@Component
class ArticlePredictionHandler(
    private val predictionCommandService: PredictionCommandService,
    private val predictionQueryService: PredictionQueryService,
    private val strategyFactory: StrategyFactory
) : PredictionHandler {

    override fun supports(type: String): Boolean = type == "article"

    override suspend fun handle(task: PredictionTask) {
        newSuspendedTransaction(
            context = Dispatchers.IO,
            transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
        ) {
            if (task !is ArticlePredictionTask) {
                throw NotSupportedTypeException("Unsupported prediction task type in Article Prediction: ${task.type}")
            }
            val tickerId = task.tickerId

            val sentimentScoreStrategy = strategyFactory.findSentimentScoreStrategy(task.type)
            if (sentimentScoreStrategy !is ArticleSentimentScoreStrategy) {
                throw NotSupportedTypeException("Unsupported sentiment score strategy in Init Prediction: ${sentimentScoreStrategy.javaClass}")
            }
            task.payload.previousScore = predictionQueryService.getTodaySentimentScore(tickerId)
            val score = sentimentScoreStrategy.calculate(task)

            predictionCommandService.updatePredictionByArticle(
                tickerId,
                task.payload.predictionDate,
                task.payload.positiveArticleCount,
                task.payload.negativeArticleCount,
                task.payload.neutralArticleCount,
                score
            )
        }
    }
}