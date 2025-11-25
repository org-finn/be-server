package finn.queryDto

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class PredictionDetailQueryDto(
    val predictionDate: LocalDateTime,
    val tickerId: UUID,
    val shortCompanyName: String,
    val tickerCode: String,
    val predictionStrategy: String,
    val sentiment: Int,
    val articleCount: Long,
    val sentimentScore: Int,
    val priceDate: LocalDate,
    val open: BigDecimal,
    val close: BigDecimal,
    val high: BigDecimal,
    val low: BigDecimal,
    val volume: Long
)