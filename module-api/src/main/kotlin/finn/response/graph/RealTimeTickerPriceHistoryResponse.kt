package finn.response.graph

import java.math.BigDecimal
import java.util.*

data class RealTimeTickerPriceHistoryResponse(
    val priceDate: String,
    val tickerId: UUID,
    val priceDataList: List<TickerRealTimeGraphResponse>,
    val maxLen: Int
) {
    data class TickerRealTimeGraphResponse(
        val price: BigDecimal,
        val hours: String,
        val index: Int
    )
}
