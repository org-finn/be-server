package finn.orchestrator

import finn.mapper.toDto
import finn.paging.PredictionPageRequest
import finn.response.prediciton.PredictionDetailResponse
import finn.response.prediciton.PredictionListResponse
import finn.service.ArticleQueryService
import finn.service.PredictionQueryService
import finn.transaction.ExposedTransactional
import org.springframework.stereotype.Service
import java.util.*

@Service
@ExposedTransactional(readOnly = true)
class PredictionOrchestrator(
    private val predictionQueryService: PredictionQueryService,
    private val articleQueryService: ArticleQueryService
) {

    fun getRecentPredictionList(pageRequest: PredictionPageRequest): PredictionListResponse {
        val predictionList = predictionQueryService.getPredictionList(pageRequest)
        return toDto(predictionList)
    }

    fun getPredictionDetail(tickerId: UUID): PredictionDetailResponse {
        val predictionDetail = predictionQueryService.getPredictionDetail(tickerId)
        val articleList = articleQueryService.getArticleDataForPredictionDetail(tickerId)
        return toDto(predictionDetail, articleList)
    }
}