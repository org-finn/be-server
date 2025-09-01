package finn.orchestrator

import finn.mapper.toDto
import finn.response.graph.TickerGraphResponse
import finn.response.graph.TickerRealTimeGraphListResponse
import finn.response.graph.TickerRealTimeGraphListResponse.TickerRealTimeGraphResponse
import finn.s3.S3Service
import finn.service.GraphQueryService
import finn.transaction.ExposedTransactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
@ExposedTransactional(readOnly = true)
class TickerPriceOrchestrator(
    private val graphQueryService: GraphQueryService,
    private val s3Service: S3Service,
    @Value("\${cloud.aws.s3.bucket}")
    private val bucketName: String
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