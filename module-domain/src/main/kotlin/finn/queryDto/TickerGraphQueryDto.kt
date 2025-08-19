package finn.queryDto

import java.math.BigDecimal
import java.time.LocalDate

interface TickerGraphQueryDto {

    fun date(): LocalDate

    fun price(): BigDecimal

    fun changeRate(): BigDecimal
}