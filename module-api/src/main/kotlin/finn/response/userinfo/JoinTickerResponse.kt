package finn.response.userinfo

data class JoinTickerResponse(
    val tickers: List<JoinTicker>,
    val pageNumber: Int,
    val hasNext: Boolean
) {
    data class JoinTicker(
        val tickerCode: String,
        val shortCompanyName: String,
        val predictionStrategy: String,
    )
}
