package finn.queryDto

import finn.entity.query.PredictionStrategy
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
    var sentiment: Int,
    var strategy: PredictionStrategy
)
