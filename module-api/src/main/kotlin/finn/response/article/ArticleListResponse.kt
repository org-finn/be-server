package finn.response.article

import java.util.*

data class ArticleListResponse(
    val articleList: List<ArticleDataResponse>,
    val pageNumber: Int,
    val hasNext: Boolean
) {
    data class ArticleDataResponse(
        val articleId: UUID,
        val title: String,
        val description: String,
        val shortCompanyNames: List<String>? = emptyList(),
        val thumbnailUrl: String? = null,
        val contentUrl: String,
        val publishedDate: String,
        val source: String,
    )
}
