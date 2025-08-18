package finn.moduleDomain.queryDto

import java.util.*

interface TickerSearchQueryDto {

    fun getTickerId(): UUID

    fun getTickerCode(): String

    fun getShortCompanyName(): String

    fun getFullCompanyName(): String
}