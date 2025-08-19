package finn.repository

import finn.entity.Prediction
import finn.paging.PageResponse
import finn.queryDto.PredictionDetailQueryDto
import java.util.*

interface PredictionRepository {
    fun getPredictionList(page: Int, size: Int, sort: String): PageResponse<Prediction>

    fun getPredictionDetail(tickerId: UUID): PredictionDetailQueryDto
}