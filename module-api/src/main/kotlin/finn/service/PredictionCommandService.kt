package finn.service

import finn.converter.SentimentConverter
import finn.queryDto.PredictionCreateDto
import finn.queryDto.PredictionUpdateDto
import finn.repository.PredictionRepository
import org.springframework.stereotype.Service

@Service
class PredictionCommandService(
    private val sentimentConverter: SentimentConverter,
    private val predictionRepository: PredictionRepository,
) {

    suspend fun createPredictions(newPredictions: List<PredictionCreateDto>) {
        setStrategyAndSentimentForInit(newPredictions)
        predictionRepository.saveAll(newPredictions)
    }

    suspend fun updatePredictions(
        predictions: List<PredictionUpdateDto>,
        alpha: Double
    ) {
        predictionRepository.updateAll(
            predictions, alpha
        )
    }

    private fun setStrategyAndSentimentForInit(newPredictions: List<PredictionCreateDto>) {
        newPredictions.forEach {
            it.strategy = sentimentConverter.getStrategyFromScore(it.score)
            it.sentiment = sentimentConverter.getSentiment(it.strategy)
        }
    }

}