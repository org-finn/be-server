package finn.controller

import finn.apiSpec.TickerPriceApiSpec
import finn.orchestrator.TickerPriceOrchestrator
import finn.response.SuccessResponse
import finn.response.graph.TickerGraphResponse
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class TickerPriceController(
    private val tickerPriceOrchestrator: TickerPriceOrchestrator
) : TickerPriceApiSpec {

    override fun getGraphData(
        tickerId: UUID,
        period: String
    ): SuccessResponse<TickerGraphResponse> {
        val response = tickerPriceOrchestrator.getTickerGraphData(tickerId, period)
        return SuccessResponse("200 OK", "종목 그래프 데이터를 성공적으로 조회하였습니다.", response)
    }
}