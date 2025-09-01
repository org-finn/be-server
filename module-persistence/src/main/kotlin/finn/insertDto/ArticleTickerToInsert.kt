package finn.insertDto

import java.util.*

data class ArticleTickerToInsert(
    val articleId: UUID,
    val tickerId: UUID,
    val title: String,
    val sentiment: String? = null,
    val reasoning: String? = null
)
