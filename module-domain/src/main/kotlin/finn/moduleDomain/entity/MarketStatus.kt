package finn.moduleDomain.entity

import finn.moduleDomain.converter.BusinessDayLocalizer
import java.time.DayOfWeek
import java.time.LocalDate

class MarketStatus private constructor(
    val date: LocalDate,
    val tradingHours: String,
    val eventName: String? = null
) {
    companion object {
        fun create(date: LocalDate, tradingHours: String, eventName: String?): MarketStatus {
            return MarketStatus(date, tradingHours, eventName)
        }

        fun isWeekend(date: LocalDate): Boolean {
            val dayOfWeek = date.getDayOfWeek()
            return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY
        }

        fun getWeekendMarketStatus(date: LocalDate): MarketStatus {
            return MarketStatus(date, getClosedDayTradingHours(), "주말")
        }

        fun getFullOpenedMarketStatus(
            date: LocalDate,
            localizer: BusinessDayLocalizer
        ): MarketStatus {
            return MarketStatus(date, localizer.getTradingHours(), null)
        }

        fun getClosedDayTradingHours(): String {
            return "휴장"
        }
    }
}