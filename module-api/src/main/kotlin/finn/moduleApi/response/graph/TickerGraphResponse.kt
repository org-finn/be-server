package finn.moduleApi.response.graph

import java.math.BigDecimal

data class TickerGraphResponse(
    val period: String,
    val graphData : List<TickerGraphResponse>
) {
    data class TickerGraphDataResponse(
        val date : String,
        val price : BigDecimal
    )
}
