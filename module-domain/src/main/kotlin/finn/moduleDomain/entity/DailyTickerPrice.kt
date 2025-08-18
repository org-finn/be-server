package finn.moduleDomain.entity

import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

class DailyTickerPrice private constructor(
    val open: BigDecimal,
    val close: BigDecimal,
    val high: BigDecimal,
    val low: BigDecimal,
    val volume: Long,
    val priceDate: LocalDate,
    val tickerId: UUID,
    val tickerCode: String
) {
    companion object {
        fun create(
            open: BigDecimal,
            close: BigDecimal,
            high: BigDecimal,
            low: BigDecimal,
            volume: Long,
            priceDate: LocalDate,
            tickerId: UUID,
            tickerCode: String
        ): DailyTickerPrice {
            return DailyTickerPrice(open, close, high, low, volume, priceDate, tickerId, tickerCode)
        }
    }
}