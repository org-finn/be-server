package finn.entity.query

import java.time.LocalDateTime
import java.util.*

class PredictionQ private constructor(
    val tickerId: UUID,
    val tickerCode: String,
    val shortCompanyName: String,
    val positiveArticleCount: Long,
    val negativeArticleCount: Long,
    val neutralArticleCount: Long,
    val sentimentScore: Int,
    val sentiment: Int,
    val predictionStrategy: PredictionStrategy,
    val predictionDate: LocalDateTime
) {

    companion object {
        fun create(
            tickerId: UUID, tickerCode: String, shortCompanyName: String,
            positiveArticleCount: Long, negativeArticleCount: Long,
            neutralArticleCount: Long, sentimentScore: Int, sentiment: Int,
            strategy: String, predictionDate: LocalDateTime
        ): PredictionQ {
            return PredictionQ(
                tickerId = tickerId,
                tickerCode = tickerCode,
                shortCompanyName = shortCompanyName,
                positiveArticleCount = positiveArticleCount,
                negativeArticleCount = negativeArticleCount,
                neutralArticleCount = neutralArticleCount,
                sentimentScore = sentimentScore,
                sentiment = sentiment,
                predictionStrategy = PredictionStrategy.findByStrategy(strategy),
                predictionDate = predictionDate
            )
        }
    }

    fun update(
        positiveArticleCount: Long, negativeArticleCount: Long,
        neutralArticleCount: Long, sentimentScore: Int, sentiment: Int,
        strategy: String
    ): PredictionQ {
        return create(
            this.tickerId,
            this.tickerCode,
            this.shortCompanyName,
            positiveArticleCount,
            negativeArticleCount,
            neutralArticleCount,
            sentimentScore,
            sentiment,
            strategy,
            this.predictionDate
        )
    }
}