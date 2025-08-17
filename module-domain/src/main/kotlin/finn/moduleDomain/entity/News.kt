package finn.moduleDomain.entity

import java.time.LocalDateTime
import java.util.*

class News(
    val id: UUID,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val contentUrl: String,
    val publishedDate: LocalDateTime,
    val shortCompanyName: String,
    val source: String,
    val sentiment: Int,
    val reasoning: String? = null,
    val tickerId: UUID
) {

    fun convertSentimentToInt(sentimentString: String): Int {
        if (sentimentString == "positive") {
            return 1 // 긍정
        } else if (sentimentString == "negative") {
            return -1 // 부정
        }
        return 0 // 중립
    }

}