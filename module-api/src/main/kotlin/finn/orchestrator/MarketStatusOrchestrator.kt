package finn.orchestrator

import finn.response.marketstatus.TodayMarketStatusResponse
import finn.service.MarketStatusQueryService
import finn.transaction.ExposedTransactional
import org.springframework.stereotype.Service

@Service
@ExposedTransactional(readOnly = true)
class MarketStatusOrchestrator(
    private val marketStatusQueryService: MarketStatusQueryService
) {

    fun getTodayMarketStatus(): TodayMarketStatusResponse {
        val marketStatus = marketStatusQueryService.getTodayMarketStatus()
        return TodayMarketStatusResponse(
            marketStatus.checkIsClosedDay(),
            marketStatus.tradingHours,
            marketStatus.eventName
        )
    }

}