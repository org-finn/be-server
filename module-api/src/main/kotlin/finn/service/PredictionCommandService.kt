package finn.service

import finn.converter.SentimentConverter
import finn.queryDto.PredictionCreateDto
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

    suspend fun createPredictions(newPredictions: List<PredictionCreateDto>) {
        setStrategyAndSentimentForInit(newPredictions)
        predictionRepository.saveAll(newPredictions)
    }

    suspend fun updatePredictions(
        predictions: List<PredictionUpdateDto>,
    ) {
        predictionRepository.updateAll(
            predictions
        )
    }

    private fun setStrategyAndSentimentForInit(newPredictions: List<PredictionCreateDto>) {
        newPredictions.forEach {
            it.strategy = sentimentConverter.getStrategyFromScore(it.score)
            it.sentiment = sentimentConverter.getSentiment(it.strategy)
        }
    }

}