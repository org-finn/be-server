package finn.request.lambda

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

data class LambdaArticleRealTimeRequest(
    val article: LambdaArticle,

    @field:JsonProperty("is_market_open")
    val isMarketOpen: Boolean,

    @field:JsonProperty("created_at")
    val createdAt: OffsetDateTime
) {
    data class LambdaArticle(
        @field:JsonProperty("published_date")
        val publishedDate: OffsetDateTime, // Timezone 정보 포함

        val title: String,

        val description: String,

        @field:JsonProperty("article_url")
        val articleUrl: String,

        @field:JsonProperty("thumbnail_url")
        val thumbnailUrl: String?,

        val author: String,

        @field:JsonProperty("distinct_id")
        val distinctId: String,

        val tickers: List<String>? = null,

        val insights: List<ArticleRealTimeInsightRequest>
    ) {
        data class ArticleRealTimeInsightRequest(
            @field:JsonProperty("ticker_code")
            val tickerCode: String,
            val sentiment: String,
            val reasoning: String
        )
    }
}