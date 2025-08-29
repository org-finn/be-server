package finn.service

import finn.entity.command.ArticleC
import finn.entity.command.PredictionC
import finn.repository.PredictionRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.*

@Service
class PredictionCommandService(
    private val predictionRepository: PredictionRepository,
) {

    fun savePrediction(
        articleList: List<ArticleC>,
        tickerId: UUID,
        tickerCode: String,
        shortCompanyName: String,
        predictionDate: OffsetDateTime
    ) {
        val positiveArticleCount = ArticleC.getPositiveCount(articleList)
        val negativeArticleCount = ArticleC.getNegativeCount(articleList)
        val neutralArticleCount = ArticleC.getNeutralCount(articleList)
        val predictionDate = predictionDate.toLocalDateTime() // 담긴 날짜 그대로 반환
        val todayScores = predictionRepository.getRecentSentimentScoreList(tickerId)

        val predictionQ = PredictionC.create(
            tickerId,
            tickerCode,
            shortCompanyName,
            positiveArticleCount,
            negativeArticleCount,
            neutralArticleCount,
            predictionDate,
            todayScores
        )

        predictionRepository.savePrediction(predictionQ)
    }
}