package finn.queryDto

import java.util.*

data class PredictionArticleDataQueryDto(
    val articleId: UUID,
    val tickerId: UUID,
    val headline: String,
    val sentiment: String?,
    val reasoning: String?
)