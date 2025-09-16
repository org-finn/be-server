package finn.queryDto

import java.util.*

interface TickerSearchQueryDto {

    fun tickerId(): UUID

    fun tickerCode(): String

    fun shortCompanyName(): String

    fun shortCompanyNameKr(): String

    fun fullCompanyName(): String
}