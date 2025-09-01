package finn.request.lambda

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import java.util.*

data class LambdaPredictionRequest(
    @field:JsonProperty("ticker_code")
    val tickerCode: String,

    @field:JsonProperty("ticker_id")
    val tickerId: UUID,

    @field:JsonProperty("short_company_name")
    val shortCompanyName: String,

    @field:JsonProperty("positive_news_count")
    val positiveNewsCount: Long,

    @field:JsonProperty("negative_news_count")
    val negativeNewsCount: Long,

    @field:JsonProperty("neutral_news_count")
    val neutralNewsCount: Long,

    @field:JsonProperty("prediction_date")
    val predictionDate: OffsetDateTime,

    @field:JsonProperty("created_at")
    val createdAt: OffsetDateTime
)
