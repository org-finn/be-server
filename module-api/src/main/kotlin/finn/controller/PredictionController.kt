package finn.controller

import finn.apiSpec.PredictionApiSpec
import finn.auth.OptionalAuth
import finn.orchestrator.PredictionOrchestrator
import finn.paging.PredictionPageRequest
import finn.response.SuccessResponse
import finn.response.prediciton.PredictionDetailResponse
import finn.response.prediciton.PredictionListResponse
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class PredictionController(
    private val predictionOrchestrator: PredictionOrchestrator
) : PredictionApiSpec {

    @OptionalAuth
    override fun getTickerPredictionList(
        pageRequest: PredictionPageRequest,
        userId: UUID?
    ): SuccessResponse<PredictionListResponse> {
        val response = predictionOrchestrator.getRecentPredictionList(pageRequest, userId)
        return SuccessResponse("200 OK", "종목 예측 목록을 성공적으로 조회하였습니다.", response)
    }

    override fun getTickerPredictionDetail(tickerId: UUID): SuccessResponse<PredictionDetailResponse> {
        val response = predictionOrchestrator.getPredictionDetail(tickerId)
        return SuccessResponse("200 OK", "종목 예측 상세 정보를 성공적으로 조회하였습니다.", response)
    }
}