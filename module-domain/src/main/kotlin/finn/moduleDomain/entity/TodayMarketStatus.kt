package finn.moduleDomain.entity

import java.time.DayOfWeek
import java.time.LocalDate

class TodayMarketStatus(
    val date: LocalDate,
    val tradingHours : String,
    val eventName : String
) {
    fun isWeekend(date: LocalDate): Boolean {
        val dayOfWeek = date.getDayOfWeek()
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY
    }

    fun isCompletedClosedDay(): Boolean {
        return tradingHours == "휴장"
    }
}