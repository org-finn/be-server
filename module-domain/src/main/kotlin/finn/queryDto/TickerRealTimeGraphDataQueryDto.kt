package finn.queryDto

import java.math.BigDecimal

data class TickerRealTimeGraphDataQueryDto(
    val price: BigDecimal,
    val hours: String,
    val index: Int
)