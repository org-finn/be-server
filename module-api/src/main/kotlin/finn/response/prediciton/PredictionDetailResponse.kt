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
    val articleCount: Long,
    val sentimentScore: Int,
    val detailData: PredictionDetailDataResponse,
    val isFavorite: Boolean?,
) {
    data class PredictionDetailDataResponse(
        val priceDate: String,
        val open: BigDecimal,
        val close: BigDecimal,
        val high: BigDecimal,
        val low: BigDecimal,
        val volume: Long,
        val article: List<PredictionArticleDataResponse>
    ) {
        data class PredictionArticleDataResponse(
            val articleId: UUID,
            val headline: String,
            val sentiment: String? = null,
            val reasoning: String? = null
        )
    }
}
