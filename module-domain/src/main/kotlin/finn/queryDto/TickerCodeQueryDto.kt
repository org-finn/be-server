package finn.queryDto

data class TickerCodeQueryDto(
    val tickerCodes: List<TickerCodes>
) {
    data class TickerCodes(
        val tickerCode: String,
        val exchangeCode: String
    )
}
