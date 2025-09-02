package finn.queryDto

import java.math.BigDecimal

interface TickerRealTimeGraphDataQueryDto {

    fun price(): BigDecimal

    fun hours(): String

    fun index(): Int
}