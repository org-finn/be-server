package finn.request.lambda

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

data class ArticleRealTimeRequest(
    @field:JsonProperty("published_date")
    val publishedDate: OffsetDateTime, // Timezone 정보 포함

    val title: String,

    val description: String,

    val articleUrl: String,

    val thumbnailUrl: String?,

    val author: String,

    val distinctId: String,

    val tickerId: String?,

    val tickerCode: String?,

    val shortCompanyName: String?,

    val createdAt: String,

    val isMarketOpen: Boolean
)