package finn.response.userinfo

import finn.response.prediciton.PredictionListResponse.PredictionListGraphDataResponse
import java.util.*

data class FavoriteTickerResponse(
    val tickers: List<FavoriteTicker>
) {
    data class FavoriteTicker(
        val tickerId: UUID,
        val tickerCode: String,
        val shortCompanyName: String,
        val predictionStrategy: String,
        val sentiment: Int,
        val graphData: PredictionListGraphDataResponse?
    )
}
