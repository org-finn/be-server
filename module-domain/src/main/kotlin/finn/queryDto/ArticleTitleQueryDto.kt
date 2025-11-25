package finn.queryDto

import java.util.*

data class ArticleTitleQueryDto(
    val articleId: UUID,
    val title: String
)