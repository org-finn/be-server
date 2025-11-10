package finn.insertDto

import java.time.LocalDateTime

data class ArticleToInsert(
    val title: String,
    val titleKr: String,
    val description: String,
    var descriptionKr: String,
    val thumbnailUrl: String? = null,
    val contentUrl: String,
    val publishedDate: LocalDateTime,
    val source: String,
    val distinctId: String,
    val tickers: String? = null
)
