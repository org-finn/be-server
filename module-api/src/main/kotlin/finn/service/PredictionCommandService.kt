package finn.service

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
        tickerId: UUID,
        tickerCode: String,
        shortCompanyName: String,
        predictionDate: OffsetDateTime,
        positiveArticleCount : Long,
        negativeArticleCount : Long,
        neutralArticleCount : Long
    ) {
        val recentScores = predictionRepository.getRecentSentimentScoreList(tickerId)

        val predictionC = PredictionC.create(
            tickerId,
            tickerCode,
            shortCompanyName,
            positiveArticleCount,
            negativeArticleCount,
            neutralArticleCount,
            predictionDate.toLocalDateTime(),
            recentScores
        )

        predictionRepository.updatePrediction(predictionC)
    }
}