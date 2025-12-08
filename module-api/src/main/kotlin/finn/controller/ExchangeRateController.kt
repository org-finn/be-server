package finn.controller

import finn.apiSpec.ExchangeRateApiSpec
import finn.orchestrator.ExchangeRateOrchestrator
import finn.response.SuccessResponse
import finn.response.exchangeRate.ExchangeRateRealTimeResponse
import org.springframework.web.bind.annotation.RestController

@RestController
class ExchangeRateController(
    private val exchangeRateOrchestrator: ExchangeRateOrchestrator
) : ExchangeRateApiSpec {

    override fun getExchangeRateRealTime(indexCode: String): SuccessResponse<ExchangeRateRealTimeResponse> {
        val response = exchangeRateOrchestrator.getExchangeRateRealTime(indexCode)
        return SuccessResponse("200 OK", "실시간 환율 조회에 성공하였습니다.", response)
    }
}