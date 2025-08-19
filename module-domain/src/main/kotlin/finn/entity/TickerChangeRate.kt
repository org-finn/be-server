package finn.entity

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

class TickerChangeRate private constructor(
    interval: Int,
    priceDate: LocalDateTime,
    changeRate: BigDecimal,
    tickerCode: String,
    tickerId: UUID
) {
    companion object {
        fun create(
            interval: Int,
            priceDate: LocalDateTime,
            changeRate: BigDecimal,
            tickerCode: String,
            tickerId: UUID
        ): TickerChangeRate {
            return TickerChangeRate(interval, priceDate, changeRate, tickerCode, tickerId)
        }
    }
}