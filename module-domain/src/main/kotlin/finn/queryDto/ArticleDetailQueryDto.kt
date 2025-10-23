package finn.queryDto

import java.time.ZonedDateTime
import java.util.*

interface ArticleDetailQueryDto {
    fun articleId(): UUID

    fun headline(): String

    fun description(): String

    fun thumbnailUrl(): String?

    fun contentUrl(): String

    fun publishedDate(): ZonedDateTime

    fun source(): String

    fun tickers(): List<ArticleDetailTickerQueryDto>?

}