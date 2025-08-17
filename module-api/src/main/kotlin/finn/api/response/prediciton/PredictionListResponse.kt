package finn.api.response.prediciton

data class PredictionListResponse(
    val predictionDate: String,
    val tickerList: List<PredictionDataResponse>,
    val pageNumber: Int,
    val hasNext: Boolean
) {
    data class PredictionDataResponse(
        val tickerId: String,
        val shortCompanyName: String,
        val tickerCode: String,
        val predictionStrategy: String,
        val sentiment: Int,
        val newsCount: Int
    )
}
