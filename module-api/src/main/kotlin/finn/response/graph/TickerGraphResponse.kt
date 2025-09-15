package finn.response.graph

import java.math.BigDecimal

data class TickerGraphResponse(
    val period: String,
    val graphData: List<TickerGraphDataResponse>
) {
    data class TickerGraphDataResponse(
        val date: String,
        val price: BigDecimal,
        val changeRate: BigDecimal,
        val positiveArticleCount: Long,
        val negativeArticleCount: Long
    )
}
