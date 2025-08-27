package finn.service

import finn.entity.Article
import finn.entity.Prediction
import finn.repository.PredictionRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class PredictionCommandService(
    private val predictionRepository: PredictionRepository,
) {

    fun savePrediction(
        articleList: List<Article>,
        tickerId: UUID,
        tickerCode: String,
        shortCompanyName: String
    ) {
        val positiveArticleCount = Article.getPositiveCount(articleList)
        val negativeArticleCount = Article.getNegativeCount(articleList)
        val neutralArticleCount = Article.getNeutralCount(articleList)
        val predictionDate = LocalDateTime.now()
        val todayScores = predictionRepository.getRecentSentimentScore(tickerId)

        val prediction = Prediction.create(
            tickerId,
            tickerCode,
            shortCompanyName,
            positiveArticleCount,
            negativeArticleCount,
            neutralArticleCount,
            predictionDate,
            todayScores
        )

        predictionRepository.savePrediction(prediction)
    }
}