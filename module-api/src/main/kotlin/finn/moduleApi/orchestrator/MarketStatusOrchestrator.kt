package finn.moduleApi.orchestrator

import finn.moduleApi.response.marketstatus.TodayMarketStatusResponse
import finn.moduleApi.service.MarketStatusQueryService
import finn.moduleCommon.transaction.Transactional
import org.springframework.stereotype.Service

@Service
@Transactional(readOnly = true)
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