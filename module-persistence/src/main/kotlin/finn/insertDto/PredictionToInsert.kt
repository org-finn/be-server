package finn.insertDto

import java.time.LocalDateTime
import java.util.*

data class PredictionToInsert(
    val tickerId: UUID,
    val tickerCode: String,
    val shortCompanyName: String,
    val positiveArticleCount: Long,
    val negativeArticleCount: Long,
    val neutralArticleCount: Long,
    val sentimentScore: Int,
    val sentiment: Int,
    val strategy: String,
    val predictionDate: LocalDateTime
)
