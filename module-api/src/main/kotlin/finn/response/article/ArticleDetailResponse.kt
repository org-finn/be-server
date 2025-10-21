package finn.response.article

import java.util.*

data class ArticleDetailResponse(
    val articleId: UUID,
    val title: String,
    val description: String,
    val thumbnailUrl: String?,
    val contentUrl: String,
    val publishedDate: String,
    val source: String,
    val tickers: List<ArticleDetailTickerResponse>?
) {
    data class ArticleDetailTickerResponse(
        val shortCompanyName: String,
        val sentiment: String?,
        val reasoning: String?
    )
}
