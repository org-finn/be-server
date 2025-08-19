package finn.repository.impl

import finn.entity.Prediction
import finn.mapper.toDomain
import finn.paging.PageResponse
import finn.queryDto.PredictionDetailQueryDto
import finn.repository.PredictionRepository
import finn.repository.exposed.PredictionExposedRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class PredictionRepositoryImpl(
    private val predictionExposedRepository: PredictionExposedRepository
) : PredictionRepository {
    override fun getPredictionList(
        page: Int,
        size: Int,
        sort: String
    ): PageResponse<Prediction> {
        val predictionExposedList = when (sort) {
            "upward" -> predictionExposedRepository.findALlPredictionBySentimentScore(
                page,
                size,
                false
            )
            "downward" -> predictionExposedRepository.findALlPredictionBySentimentScore(
                page,
                size,
                true
            )
            else -> predictionExposedRepository.findALlPredictionByPopular(page, size)
        }
        return PageResponse(predictionExposedList.content.map { it ->
            toDomain(it)
        }.toList(), page, size, predictionExposedList.hasNext)
    }

    override fun getPredictionDetail(tickerId: UUID): PredictionDetailQueryDto {
        return  predictionExposedRepository.findPredictionWithPriceInfoById(tickerId)
    }
}