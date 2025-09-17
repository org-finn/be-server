package finn.service

import finn.converter.SentimentConverter
import finn.repository.PredictionRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.*

@Service
class PredictionCommandService(
    private val sentimentConverter: SentimentConverter,
    private val predictionRepository: PredictionRepository,
) {

    fun createPrediction(
        tickerId: UUID,
        tickerCode: String,
        shortCompanyName: String,
        score: Int,
        predictionDate: OffsetDateTime
    ) {
        val strategy = sentimentConverter.getStrategyFromScore(score)
        val sentiment = sentimentConverter.getSentiment(strategy)
        predictionRepository.save(
            tickerId,
            tickerCode,
            shortCompanyName,
            sentiment,
            strategy,
            score,
            predictionDate.toLocalDateTime()
        )
    }

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