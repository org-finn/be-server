package finn.response.prediciton

import java.math.BigDecimal
import java.util.*

data class PredictionListResponse(
    val predictionDate: String,
    val predictionList: List<PredictionDataResponse>,
    val pageNumber: Int,
    val hasNext: Boolean
) {
    data class PredictionDataResponse(
        val tickerId: UUID,
        val shortCompanyName: String,
        val tickerCode: String,
        val predictionStrategy: String,
        val sentiment: Int,
        val articleCount: Long,
        // param = keyword
        val positiveKeywords: String?,
        val negativeKeywords: String?,
        val isFavorite: Boolean?,
        // param = article
        val articleTitles: List<ArticleTitleResponse>?,
        // param = graph
        val graphData: PredictionListGraphDataResponse?
    )

    data class ArticleTitleResponse(
        val articleId: UUID,
        val title: String
    )

    data class PredictionListGraphDataResponse(
        val isMarketOpen: Boolean,
        val priceData: List<BigDecimal>
    )
}
