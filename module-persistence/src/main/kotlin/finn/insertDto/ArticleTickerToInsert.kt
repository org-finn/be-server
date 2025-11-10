package finn.insertDto

import java.time.LocalDateTime
import java.util.*

data class ArticleTickerToInsert(
    val articleId: UUID,
    val tickerId: UUID,
    val tickerCode: String,
    val title: String,
    val titleKr: String,
    val sentiment: String? = null,
    val reasoning: String? = null,
    val reasoningKr: String? = null,
    val publishedDate: LocalDateTime
)
