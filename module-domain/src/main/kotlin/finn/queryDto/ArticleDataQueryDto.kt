package finn.queryDto

import java.util.*

data class ArticleDataQueryDto(
    val articleId: UUID,
    val tickerId: UUID,
    val headline: String,
    val sentiment: String?,
    val reasoning: String?
)