package finn.orchestrator

import finn.mapper.toDto
import finn.response.graph.TickerGraphResponse
import finn.response.graph.TickerRealTimeGraphResponse
import finn.s3.S3Service
import finn.service.GraphQueryService
import finn.transaction.ExposedTransactional
import org.springframework.stereotype.Service
import java.util.*

@Service
@ExposedTransactional(readOnly = true)
class TickerPriceOrchestrator(
    private val graphQueryService: GraphQueryService,
    private val s3Service: S3Service
) {
    fun getTickerGraphData(tickerId: UUID, period: String): TickerGraphResponse {
        val graphData = graphQueryService.getTickerGraphData(tickerId, period)
        return toDto(period, graphData)
    }

    fun getTickerRealTimeGraphData(tickerId: UUID): TickerRealTimeGraphResponse {
        val priceUrl = s3Service.
    }
}