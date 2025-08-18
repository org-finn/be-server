package finn.moduleDomain.entity

import java.time.LocalDateTime
import java.util.*

class News private constructor(
    val id: UUID,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val contentUrl: String,
    val publishedDate: LocalDateTime,
    val shortCompanyName: String,
    val source: String,
    val sentiment: String,
    val reasoning: String? = null,
    val tickerId: UUID
) {
    companion object {
        fun create(
            id: UUID,
            title: String,
            description: String,
            thumbnailUrl: String,
            contentUrl: String,
            publishedDate: LocalDateTime,
            shortCompanyName: String,
            source: String,
            sentiment: String,
            reasoning: String?,
            tickerId: UUID
        ): News {
            return News(
                id,
                title,
                description,
                thumbnailUrl,
                contentUrl,
                publishedDate,
                shortCompanyName,
                source,
                sentiment,
                reasoning,
                tickerId
            )
        }
    }

}