package finn.controller

import finn.apiSpec.PredictionApiSpec
import finn.orchestrator.PredictionOrchestrator
import finn.paging.PredictionPageRequest
import finn.response.SuccessResponse
import finn.response.graph.TickerGraphResponse
import finn.response.prediciton.PredictionDetailResponse
import finn.response.prediciton.PredictionListResponse
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class PredictionController(
    private val predictionOrchestrator: PredictionOrchestrator
) : PredictionApiSpec {

    override fun getTickerPredictionList(pageRequest: PredictionPageRequest): SuccessResponse<PredictionListResponse> {
        val response = predictionOrchestrator.getRecentPredictionList(pageRequest)
        return SuccessResponse("200 OK", "종목 예측 목록을 성공적으로 조회하였습니다.", response)
    }

    override fun getTickerPredictionDetail(tickerId: UUID): SuccessResponse<PredictionDetailResponse> {
        val response = predictionOrchestrator.getPredictionDetail(tickerId)
        return SuccessResponse("200 OK", "종목 예측 상세 정보를 성공적으로 조회하였습니다.", response)
    }

    override fun getGraphData(
        tickerId: UUID,
        period: String
    ): SuccessResponse<TickerGraphResponse> {
        val response = predictionOrchestrator.getTickerGraphData(tickerId, period)
        return SuccessResponse("200 OK", "종목 그래프 데이터를 성공적으로 조회하였습니다.", response)
    }
}