package finn.queryDto

import kotlinx.serialization.Serializable

@Serializable
data class ArticleIdAndTitleQueryDto(
    val articleId: String,
    val title: String
)
