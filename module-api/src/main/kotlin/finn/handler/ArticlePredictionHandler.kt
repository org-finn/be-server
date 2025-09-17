package finn.handler

import finn.score.PredictionTask
import finn.score.strategy.StrategyFactory
import finn.service.PredictionCommandService
import finn.transaction.ExposedTransactional
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class ArticlePredictionHandler(
    private val predictionService: PredictionCommandService,
    private val strategyFactory: StrategyFactory
) : PredictionHandler {

    override fun supports(type: String): Boolean = type == "article"

    @ExposedTransactional
    override suspend fun handle(task: PredictionTask) {
        val tickerId = task.tickerId

        val strategy = strategyFactory.findStrategy(task.type)
        task.payload["previousScore"] = predictionService.getTodaySentimentScore(tickerId)
        val score = strategy.calculate(task)

        predictionService.updatePredictionByArticle(
            tickerId,
            OffsetDateTime.parse(task.payload["predictionDate"] as String),
            (task.payload["positiveArticleCount"] as Int).toLong(),
            (task.payload["negativeArticleCount"] as Int).toLong(),
            (task.payload["neutralArticleCount"] as Int).toLong(),
            score
        )
    }
}