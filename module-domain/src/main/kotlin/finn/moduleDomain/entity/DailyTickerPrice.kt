package finn.moduleDomain.entity

import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

class DailyTickerPrice(
    val open: BigDecimal,
    val close: BigDecimal,
    val high: BigDecimal,
    val low: BigDecimal,
    val volume: Long,
    val priceDate: LocalDate,
    val tickerId: UUID,
    val tickerCode: String
) {
}