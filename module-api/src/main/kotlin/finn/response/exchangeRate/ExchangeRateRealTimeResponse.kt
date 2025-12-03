package finn.response.exchangeRate

import java.math.BigDecimal

data class ExchangeRateRealTimeResponse(
    val date: String,
    val indexInfo: String,
    val value: BigDecimal,
    val changeRate: BigDecimal
)
