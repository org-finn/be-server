package finn.queryDto

import java.util.*

interface TickerFilteringQueryDto {

    fun tickerId(): UUID

    fun shortCompanyName(): String
}