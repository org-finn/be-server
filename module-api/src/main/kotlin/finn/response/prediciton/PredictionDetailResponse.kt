package finn.response.prediciton

import java.math.BigDecimal
import java.util.*

data class PredictionDetailResponse(
    val predictionDate: String,
    val tickerId: UUID,
    val shortCompanyName: String,
    val tickerCode: String,
    val predictionStrategy: String,
    val sentiment: Int,
    val newsCount: Long,
    val sentimentScore: Int,
    val detailData: PredictionDetailDataResponse
) {
    data class PredictionDetailDataResponse(
        val priceDate: String,
        val open: BigDecimal,
        val close: BigDecimal,
        val high: BigDecimal,
        val low: BigDecimal,
        val volume: Long,
        val news: List<NewsDataResponse>
    ) {
        data class NewsDataResponse(
            val newsId: UUID,
            val headline: String,
            val sentiment: String,
            val reasoning: String? = null
        )
    }
}
