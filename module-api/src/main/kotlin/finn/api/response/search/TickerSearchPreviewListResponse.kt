package finn.api.response.search

import java.util.*

data class TickerSearchPreviewListResponse(
    val tickerSearchList: List<TickerSearchPreviewResponse>
) {
    data class TickerSearchPreviewResponse(
        val tickerId: UUID,
        val tickerCode: String,
        val shortCompanyName: String,
        val fullCompanyName: String
    )
}
