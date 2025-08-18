package finn.moduleDomain.entity

import java.time.LocalDateTime
import java.util.*

class Prediction private constructor(
    val tickerId: UUID,
    val tickerCode: String,
    val shortCompanyName: String,
    val predictionStrategy: PredictionStrategy,
    val sentiment: Int,
    val newsCount: Int,
    val sentimentScore: Int,
    val predictionDate: LocalDateTime
) {

    companion object {
        fun create(
            tickerId: UUID, tickerCode: String, shortCompanyName: String,
            predictionStrategy: PredictionStrategy, sentiment: Int,
            newsCount: Int, sentimentScore: Int, predictionDate: LocalDateTime
        ): Prediction {
            return Prediction(
                tickerId, tickerCode, shortCompanyName, predictionStrategy,
                sentiment, newsCount, sentimentScore, predictionDate
            )
        }
    }

}