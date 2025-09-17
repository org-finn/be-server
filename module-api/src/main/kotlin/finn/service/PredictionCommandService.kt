package finn.service

import finn.repository.PredictionRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.*

@Service
class PredictionCommandService(
    private val predictionRepository: PredictionRepository,
) {

    fun updatePredictionByArticle(
        tickerId: UUID,
        predictionDate: OffsetDateTime,
        positiveArticleCount: Long,
        negativeArticleCount: Long,
        neutralArticleCount: Long,
        score: Int
    ) {
        predictionRepository.updatePredictionByArticle(
            tickerId, predictionDate.toLocalDateTime(),
            positiveArticleCount, negativeArticleCount, neutralArticleCount, score
        )
    }

    fun getRecentSentimentScores(tickerId: UUID): List<Int> {
        return predictionRepository.getRecentSentimentScoreList(tickerId)
    }

    fun getTodaySentimentScore(tickerId: UUID): Int {
        return predictionRepository.getRecentScore(tickerId)
    }
}