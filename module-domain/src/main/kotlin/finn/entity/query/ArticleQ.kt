package finn.entity.query

import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class ArticleQ private constructor(
    val id: UUID,
    val title: String,
    val description: String,
    val thumbnailUrl: String? = null,
    val contentUrl: String,
    val publishedDate: LocalDateTime,
    val source: String,
    val tickers: List<String>? = emptyList()
) {
    companion object {
        fun create(
            id: UUID,
            title: String,
            description: String,
            thumbnailUrl: String?,
            contentUrl: String,
            publishedDate: LocalDateTime,
            source: String,
            tickers: String?
        ): ArticleQ {
            return ArticleQ(
                id,
                title,
                description,
                thumbnailUrl,
                contentUrl,
                publishedDate.atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime(), // KST 기준 적용
                source,
                tickers?.let { tickers.split(",") }
            )
        }
    }

}