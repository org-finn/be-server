package finn.response.userinfo

import java.util.*

data class FavoriteArticleResponse(
    val articles: List<FavoriteArticle>
) {
    data class FavoriteArticle(
        val articleId: UUID,
        val title: String,
        val thumbnailUrl: String?
    )
}
