package finn.queryDto

import java.util.*

data class FavoriteTickerQueryDto(
    val tickerId: UUID,
    val tickerCode: String
)
