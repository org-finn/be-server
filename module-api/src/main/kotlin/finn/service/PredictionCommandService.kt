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

    suspend fun createPrediction(
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

    suspend fun updatePredictionByArticle(
        tickerId: UUID,
        predictionDate: OffsetDateTime,
        positiveArticleCount: Long,
        negativeArticleCount: Long,
        neutralArticleCount: Long,
        score: Int,
    ) {
        val strategy = sentimentConverter.getStrategyFromScore(score)
        val sentiment = sentimentConverter.getSentiment(strategy)
        predictionRepository.updatePredictionByArticle(
            tickerId, predictionDate.toLocalDateTime(),
            positiveArticleCount, negativeArticleCount, neutralArticleCount, score,
            sentiment, strategy.strategy
        )
    }

    suspend fun getRecentSentimentScores(tickerId: UUID): List<Int> {
        return predictionRepository.getRecentSentimentScoreList(tickerId)
    }

    suspend fun getTodaySentimentScore(tickerId: UUID): Int {
        return predictionRepository.getRecentScore(tickerId)
    }
}