package finn.response.search

import finn.response.prediciton.PredictionListResponse.PredictionDataResponse

data class TickerSearchListResponse(
    val tickerSearchList: List<PredictionDataResponse>,
    val isMore: Boolean
)
