package finn.controller

import finn.apiSpec.TickerPriceApiSpec
import finn.exception.InvalidParameterException
import finn.orchestrator.TickerPriceOrchestrator
import finn.response.SuccessResponse
import finn.response.graph.RealTimeTickerPriceHistoryResponse
import finn.response.graph.TickerGraphResponse
import finn.service.TickerPriceSseService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.*

@RestController
class TickerPriceController(
    private val tickerPriceOrchestrator: TickerPriceOrchestrator,
    private val sseService: TickerPriceSseService,
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

    override fun streamTickerRealTimePrice(
        tickerId: UUID,
        response: HttpServletResponse
    ): SseEmitter {
        response.setHeader("X-Accel-Buffering", "no")
        response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate")
        response.setHeader("Connection", "keep-alive")
        return sseService.subscribe(tickerId)
    }

}