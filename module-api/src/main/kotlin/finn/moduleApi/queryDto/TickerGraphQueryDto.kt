package finn.moduleApi.queryDto

import java.math.BigDecimal
import java.time.LocalDate

interface TickerGraphQueryDto {

    fun getDate(): LocalDate

    fun getPrice(): BigDecimal

    fun getChangeRate(): BigDecimal
}