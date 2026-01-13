package finn.queryDto

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class PredictionCreateDto(
    val tickerId: UUID,
    val tickerCode: String,
    val shortCompanyName: String,
    val score: Int,
    val volatility: BigDecimal,
    val predictionDate: LocalDateTime,
    val positiveCount: Long,
    val negativeCount: Long,
    val neutralCount: Long,
    val sentiment: Int,
    val strategy: String
)
