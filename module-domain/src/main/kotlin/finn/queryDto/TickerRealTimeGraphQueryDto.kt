package finn.queryDto

import java.util.*

interface TickerRealTimeGraphQueryDto {

    fun priceDate(): String

    fun tickerId(): UUID

    fun priceDataList(): List<TickerRealTimeGraphDataQueryDto>

    fun maxLen(): Int

}