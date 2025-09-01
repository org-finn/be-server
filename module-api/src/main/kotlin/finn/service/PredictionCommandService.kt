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