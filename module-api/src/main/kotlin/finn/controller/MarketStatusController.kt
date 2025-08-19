package finn.controller

import finn.apiSpec.MarketStatusApiSpec
import finn.orchestrator.MarketStatusOrchestrator
import finn.response.SuccessResponse
import finn.response.marketstatus.TodayMarketStatusResponse
import org.springframework.web.bind.annotation.RestController

@RestController
class MarketStatusController(
    private val marketStatusOrchestrator: MarketStatusOrchestrator
) : MarketStatusApiSpec {

    override fun getTodayTickerMarketStatus(): SuccessResponse<TodayMarketStatusResponse> {
        val response = marketStatusOrchestrator.getTodayMarketStatus()
        return SuccessResponse("200 OK", "금일 주식 시장 정보를 성공적으로 조회하였습니다.", response)
    }
}