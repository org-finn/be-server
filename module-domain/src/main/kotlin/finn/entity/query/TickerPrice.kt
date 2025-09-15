package finn.entity.query

import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

class TickerPrice private constructor(
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
        ): TickerPrice {
            return TickerPrice(open, close, high, low, volume, priceDate, tickerId, tickerCode)
        }
    }
}