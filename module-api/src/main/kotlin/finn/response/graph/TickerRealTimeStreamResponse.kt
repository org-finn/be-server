package finn.response.graph

import java.math.BigDecimal

data class TickerRealTimeStreamResponse(
    val time: String,
    val open: BigDecimal,
    val high: BigDecimal,
    val low: BigDecimal,
    val close: BigDecimal,
    val volume: Long
)
