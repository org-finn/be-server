package finn.queryDto

import java.time.LocalDateTime
import java.util.*

interface ArticleDetailQueryDto {
    fun articleId(): UUID

    fun headline(): String

    fun description(): String

    fun thumbnailUrl(): String?

    fun contentUrl(): String

    fun publishedDate(): LocalDateTime

    fun source(): String

    fun tickers(): List<ArticleDetailTickerQueryDto>?

}