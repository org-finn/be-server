package finn.insertDto

import java.time.LocalDateTime
import java.util.*

data class ArticleToInsert(
    val title: String,
    val description: String,
    val thumbnailUrl: String? = null,
    val contentUrl: String,
    val publishedDate: LocalDateTime,
    val shortCompanyName: String? = null,
    val source: String,
    val distinctId: String,
    val sentiment: String? = null,
    val reasoning: String? = null,
    val tickerId: UUID? = null,
    val tickerCode: String? = null,
)
