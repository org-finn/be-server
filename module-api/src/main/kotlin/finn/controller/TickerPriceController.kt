package finn.controller

import finn.apiSpec.TickerPriceApiSpec
import finn.exception.InvalidParameterException
import finn.manager.TickerRealTimeCandleManager
import finn.orchestrator.TickerPriceOrchestrator
import finn.response.SuccessResponse
import finn.response.graph.RealTimeTickerPriceHistoryResponse
import finn.response.graph.TickerGraphResponse
import finn.service.TickerPriceSseService
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.*

@RestController
class TickerPriceController(
    private val tickerPriceOrchestrator: TickerPriceOrchestrator,
    private val sseService: TickerPriceSseService,
    private val candleManager: TickerRealTimeCandleManager
) : TickerPriceApiSpec {

    override fun getGraphData(
        tickerId: UUID,
        period: String
    ): SuccessResponse<TickerGraphResponse> {
        val response = tickerPriceOrchestrator.getTickerGraphData(tickerId, period)
        return SuccessResponse("200 OK", "종목 그래프 데이터를 성공적으로 조회하였습니다.", response)
    }

    override fun getRealTimeGraphHistoryData(
        tickerId: UUID,
        gte: Int?,
        missing: List<Int>?
    ): SuccessResponse<RealTimeTickerPriceHistoryResponse> {
        if (gte != null && missing != null) {
            throw InvalidParameterException("gte, missing 조건 중 최대 1개만 입력 가능합니다.")
        }
        val response = tickerPriceOrchestrator.getTickerRealTimeGraphData(tickerId, gte, missing)
        return SuccessResponse("200 OK", "실시간 종목 주가 데이터를 성공적으로 조회하였습니다.", response)
    }

    override fun streamTickerRealTimePrice(tickerCode: String): SseEmitter {
        return sseService.subscribe(tickerCode.uppercase())
    }

}