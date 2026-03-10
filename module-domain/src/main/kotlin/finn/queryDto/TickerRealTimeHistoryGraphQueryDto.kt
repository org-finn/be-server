package finn.queryDto

import java.util.*

data class TickerRealTimeHistoryGraphQueryDto(
    val priceDate: String,
    val tickerId: UUID,
    val priceDataList: List<TickerRealTimeGraphDataQueryDto>,
    val maxLen: Int
)