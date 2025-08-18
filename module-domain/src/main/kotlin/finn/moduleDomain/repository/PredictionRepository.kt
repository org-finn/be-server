package finn.moduleDomain.repository

import finn.moduleDomain.entity.Prediction
import finn.moduleDomain.paging.PageResponse
import finn.moduleDomain.queryDto.PredictionDetailQueryDto
import java.util.*

interface PredictionRepository {
    fun getPredictionList(page: Int, size: Int, sort: String): PageResponse<Prediction>

    fun getPredictionDetail(tickerId: UUID): PredictionDetailQueryDto
}