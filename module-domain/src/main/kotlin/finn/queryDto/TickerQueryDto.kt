package finn.queryDto

import java.util.*

data class TickerQueryDto(
    val tickerId: UUID,
    val tickerCode: String,
    val shortCompanyName: String,
    val shortCompanyNameKr: String,
    val fullCompanyName: String
)