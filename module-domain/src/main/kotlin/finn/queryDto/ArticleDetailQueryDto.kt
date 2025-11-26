package finn.queryDto

import java.time.ZonedDateTime
import java.util.*

data class ArticleDetailQueryDto(
    val articleId: UUID,
    val headline: String,
    val description: String,
    val thumbnailUrl: String?,
    val contentUrl: String,
    val publishedDate: ZonedDateTime,
    val source: String,
    val tickers: List<ArticleDetailTickerQueryDto>?
)