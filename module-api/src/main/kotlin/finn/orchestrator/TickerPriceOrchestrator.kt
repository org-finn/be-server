package finn.orchestrator

import finn.mapper.toDto
import finn.response.graph.TickerGraphResponse
import finn.response.graph.TickerRealTimeGraphListResponse
import finn.response.graph.TickerRealTimeGraphListResponse.TickerRealTimeGraphResponse
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

    fun getTickerRealTimeGraphData(tickerId: UUID): TickerRealTimeGraphListResponse {
        val dateUrlMap = s3Service.getLatestPresignedUrlsForTicker(bucketName, tickerId)
        return TickerRealTimeGraphListResponse(dateUrlMap.entries.map {
            TickerRealTimeGraphResponse(it.key, it.value.toString())
        }.toList())
    }
}