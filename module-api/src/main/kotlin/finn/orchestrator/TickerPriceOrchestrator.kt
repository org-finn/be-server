package finn.orchestrator

import finn.mapper.toDto
import finn.response.graph.RealTimeTickerPriceHistoryResponse
import finn.response.graph.TickerGraphResponse
import finn.service.GraphQueryService
import finn.transaction.ExposedTransactional
import org.springframework.stereotype.Service
import java.util.*

@Service
@ExposedTransactional(readOnly = true)
class TickerPriceOrchestrator(
    private val graphQueryService: GraphQueryService
) {
    fun getTickerGraphData(tickerId: UUID, period: String): TickerGraphResponse {
        val graphData = graphQueryService.getTickerGraphData(tickerId, period)
        return toDto(period, graphData)
    }

    fun getTickerRealTimeGraphData(tickerId: UUID, gte: Int?, missing: List<Int>?): RealTimeTickerPriceHistoryResponse {
        val graphData = graphQueryService.getTickerRealTimeGraphData(tickerId, gte, missing)
        return toDto(graphData)
    }
}