package finn.moduleApi.response.marketstatus

data class TodayMarketStatusResponse(
    val isHoliday: Boolean,
    val tradingHours: String,
    val eventName: String? = null
)
