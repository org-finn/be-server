package finn.response.article

import java.util.*

data class ArticleTickerFilteringListResponse(
    val tickerList: List<ArticleTickerFilteringResponse>
) {
    data class ArticleTickerFilteringResponse(
        val tickerId: UUID,
        val shortCompanyName: String
    )
}
