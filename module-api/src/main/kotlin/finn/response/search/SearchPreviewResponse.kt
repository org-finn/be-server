package finn.response.search

import java.util.*

data class SearchPreviewResponse(
    val tickerSearchList: List<TickerSearchPreviewResponse>,
    val articleSearchList: List<ArticleSearchPreviewResponse>
) {

    data class TickerSearchPreviewResponse(
        val tickerId: UUID,
        val tickerCode: String,
        val shortCompanyName: String,
        val fullCompanyName: String
    )

    data class ArticleSearchPreviewResponse(
        val articleId: UUID,
        val title: String
    )
}
