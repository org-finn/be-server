package finn.entity.command

import java.time.LocalDateTime

class ArticleC private constructor(
    val title: String,
    val description: String,
    val thumbnailUrl: String? = null,
    val contentUrl: String,
    val publishedDate: LocalDateTime,
    val source: String,
    val distinctId: String,
    val tickers : List<String>? = null
) {
    companion object {
        fun create(
            title: String,
            description: String,
            thumbnailUrl: String?,
            contentUrl: String,
            publishedDate: LocalDateTime,
            source: String,
            distinctId: String,
            tickers: List<String>
        ): ArticleC {
            return ArticleC(
                title,
                description,
                thumbnailUrl,
                contentUrl,
                publishedDate,
                source,
                distinctId,
                tickers
            )
        }
    }
}