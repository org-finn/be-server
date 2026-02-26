package finn.response.userinfo

import finn.response.prediciton.PredictionListResponse.PredictionListGraphDataResponse
import java.util.*

data class JoinTickerResponse(
    val tickers: List<JoinTicker>,
    val pageNumber: Int,
    val hasNext: Boolean
) {
    data class JoinTicker(
        val tickerId: UUID,
        val tickerCode: String,
        val shortCompanyName: String,
        val predictionStrategy: String,
        val sentiment: Int,
        val graphData: PredictionListGraphDataResponse?
    )
}
