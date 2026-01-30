package finn.queryDto

import java.util.*

data class FavoriteArticleQueryDto(
    val articleId: UUID,
    val title: String,
    val thumbnailUrl: String?
)
