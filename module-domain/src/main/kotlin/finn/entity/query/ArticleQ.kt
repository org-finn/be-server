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
    val shortCompanyName: String? = null,
    val source: String,
    val sentiment: String? = null,
    val reasoning: String? = null,
    val tickerId: UUID? = null
) {
    companion object {
        fun create(
            id: UUID,
            title: String,
            description: String,
            thumbnailUrl: String?,
            contentUrl: String,
            publishedDate: LocalDateTime,
            shortCompanyName: String?,
            source: String,
            sentiment: String?,
            reasoning: String?,
            tickerId: UUID?
        ): ArticleQ {
            return ArticleQ(
                id,
                title,
                description,
                thumbnailUrl,
                contentUrl,
                publishedDate.atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime(), // KST 기준 적용
                shortCompanyName,
                source,
                sentiment,
                reasoning,
                tickerId
            )
        }
    }

}