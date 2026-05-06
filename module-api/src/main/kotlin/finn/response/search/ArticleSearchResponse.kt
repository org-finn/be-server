package finn.response.search

import java.util.UUID

data class ArticleSearchListResponse(
    val articles: List<ArticleSearchResponse>
) {
    data class ArticleSearchResponse(
        val articleId: UUID,
        val title: String
    )
}
