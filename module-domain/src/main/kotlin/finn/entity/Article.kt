package finn.entity

import java.time.LocalDateTime
import java.util.*

class Article private constructor(
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
        ): Article {
            return Article(
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

        fun getPositiveCount(articleList: List<Article>): Long {
            return articleList.count({ it -> it.sentiment.equals("positive") })
                .coerceAtLeast(0).toLong()
        }

        fun getNegativeCount(articleList: List<Article>): Long {
            return articleList.count({ it -> it.sentiment.equals("negative") })
                .coerceAtLeast(0).toLong()
        }

        fun getNeutralCount(articleList: List<Article>): Long {
            // 뉴스 양이 적은 것을 고려해, 감정 정보가 없는 데이터들은 중립으로 간주
            return articleList.count({ it -> it.sentiment.isNullOrBlank() || it.sentiment.equals("neutral") })
                .coerceAtLeast(0).toLong()
        }
    }

}