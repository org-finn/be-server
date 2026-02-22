package finn.queryDto

data class TickerJoinQueryDto(
    val tickerCode: String,
    val shortCompanyName: String,
    val predictionStrategy: String
)
