package finn.queryDto

import java.time.ZonedDateTime
import java.util.*

data class ArticleDataQueryDto(
    val id: UUID,
    val title: String,
    val description: String,
    val thumbnailUrl: String? = null,
    val contentUrl: String,
    val publishedDate: ZonedDateTime,
    val source: String,
    val tickers: List<String>? = emptyList(),
    val isFavorite: Boolean?
) {
    companion object {
        fun create(
            id: UUID,
            title: String,
            description: String,
            thumbnailUrl: String?,
            contentUrl: String,
            publishedDate: ZonedDateTime,
            source: String,
            tickers: String?,
            isFavorite: Boolean?
        ): ArticleDataQueryDto {
            return ArticleDataQueryDto(
                id,
                title,
                description,
                thumbnailUrl,
                contentUrl,
                publishedDate, // KST 기준 적용
                source,
                tickers?.let { tickers.split(",") },
                isFavorite
            )
        }
    }

}