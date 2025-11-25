package finn.queryDto

import java.util.*

data class TickerFilteringQueryDto(
    val tickerId: UUID,
    val shortCompanyName: String
)