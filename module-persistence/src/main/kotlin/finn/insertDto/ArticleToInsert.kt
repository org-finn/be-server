package finn.insertDto

import java.time.LocalDateTime

data class ArticleToInsert(
    val title: String,
    val description: String,
    val thumbnailUrl: String? = null,
    val contentUrl: String,
    val publishedDate: LocalDateTime,
    val source: String,
    val distinctId: String,
    val tickers: String? = null
)
