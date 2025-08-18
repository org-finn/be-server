package finn.moduleApi.repository

import finn.moduleApi.paging.PageResponse
import finn.moduleApi.paging.PredictionPageRequest
import finn.moduleApi.queryDto.PredictionDetailQueryDto
import finn.moduleDomain.entity.Prediction
import java.util.*

interface PredictionRepository {
    fun getPredictionList(pageRequest: PredictionPageRequest) : PageResponse<Prediction>

    fun getPredictionDetail(tickerId: UUID) : PredictionDetailQueryDto
}