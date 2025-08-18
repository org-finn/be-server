package finn.moduleApi.response.prediciton

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
        val newsCount: Int
    )
}
