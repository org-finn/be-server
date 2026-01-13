package finn.service

import finn.converter.SentimentConverter
import finn.entity.TickerScore
import finn.queryDto.PredictionUpdateDto
import finn.repository.PredictionRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
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
        volatility: BigDecimal,
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
            volatility,
            predictionDate.toLocalDateTime()
        )
    }

    suspend fun updatePredictionByArticle(
        predictions: List<PredictionUpdateDto>,
    ) {
        predictionRepository.updateAll(
            predictions
        )
    }

    suspend fun updatePredictionByExponent(
        predictionDate: OffsetDateTime,
        scores: List<TickerScore>
    ) {
        scores.forEach {
            it.strategy = sentimentConverter.getStrategyFromScore(it.score)
            it.sentiment = sentimentConverter.getSentiment(it.strategy)
        }

        predictionRepository.updatePredictionByExponent(
            predictionDate.toLocalDateTime(), scores
        )
    }

}