package finn.repository

import finn.entity.command.PredictionC
import finn.paging.PageResponse
import finn.queryDto.PredictionDetailQueryDto
import finn.queryDto.PredictionQueryDto
import java.util.*

interface PredictionRepository {
    fun getPredictionList(page: Int, size: Int, sort: String): PageResponse<PredictionQueryDto>

    fun getPredictionDetail(tickerId: UUID): PredictionDetailQueryDto

    fun getRecentSentimentScore(tickerId: UUID): List<Int>

    fun savePrediction(predictionQ: PredictionC)
}