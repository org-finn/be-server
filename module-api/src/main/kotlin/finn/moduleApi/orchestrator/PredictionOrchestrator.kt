package finn.moduleApi.orchestrator

import finn.moduleApi.mapper.toDto
import finn.moduleApi.paging.PredictionPageRequest
import finn.moduleApi.response.graph.TickerGraphResponse
import finn.moduleApi.response.prediciton.PredictionDetailResponse
import finn.moduleApi.response.prediciton.PredictionListResponse
import finn.moduleApi.service.GraphQueryService
import finn.moduleApi.service.NewsQueryService
import finn.moduleApi.service.PredictionQueryService
import finn.moduleCommon.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.*

@Service
@Transactional(readOnly = true)
class PredictionOrchestrator(
    private val predictionQueryService: PredictionQueryService,
    private val newsQueryService: NewsQueryService,
    private val graphQueryService: GraphQueryService
) {

    fun getRecentPredictionList(pageRequest: PredictionPageRequest): PredictionListResponse {
        val predictionList = predictionQueryService.getPredictionList(pageRequest)
        return toDto(predictionList)
    }

    fun getPredictionDetail(tickerId: UUID): PredictionDetailResponse {
        val predictionDetail = predictionQueryService.getPredictionDetail(tickerId)
        val newsList = newsQueryService.getNewsDataForPredictionDetail(tickerId)
        return toDto(predictionDetail, newsList)
    }

    fun getTickerGraphData(tickerId: UUID, period: String): TickerGraphResponse {
        val graphData = graphQueryService.getTickerGraphData(tickerId, period)
        return toDto(period, graphData)
    }
}