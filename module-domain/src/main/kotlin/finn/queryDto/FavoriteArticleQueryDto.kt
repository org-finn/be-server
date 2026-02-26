package finn.queryDto

import java.time.ZonedDateTime
import java.util.*

data class FavoriteArticleQueryDto(
    val articleId: UUID,
    val title: String,
    val description: String,
    val shortCompanyNames: List<String>? = emptyList(),
    val thumbnailUrl: String?,
    val contentUrl: String,
    val publishedDate: ZonedDateTime,
    val source: String,
)
