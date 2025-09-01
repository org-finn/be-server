package finn.response.graph

data class TickerRealTimeGraphListResponse(
    val dataList: List<TickerRealTimeGraphResponse>
) {
    data class TickerRealTimeGraphResponse(
        val priceDate: String,
        val priceUrl: String
    )
}
