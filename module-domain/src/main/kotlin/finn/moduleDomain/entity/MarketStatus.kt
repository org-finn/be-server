package finn.moduleDomain.entity

import java.time.DayOfWeek
import java.time.LocalDate

class MarketStatus private constructor(
    val date: LocalDate,
    val tradingHours: String,
    val eventName: String
) {
    companion object {
        fun create(date: LocalDate, tradingHours: String, eventName: String): MarketStatus {
            return MarketStatus(date, tradingHours, eventName)
        }

        fun isWeekend(date: LocalDate): Boolean {
            val dayOfWeek = date.getDayOfWeek()
            return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY
        }
    }

    fun isClosedDay(): Boolean {
        return tradingHours == "휴장"
    }
}