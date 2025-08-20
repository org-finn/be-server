package finn.repository.impl

import finn.entity.Prediction
import finn.mapper.toDomain
import finn.paging.PageResponse
import finn.queryDto.PredictionDetailQueryDto
import finn.repository.PredictionRepository
import finn.repository.query.PredictionQueryRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class PredictionRepositoryImpl(
    private val predictionQueryRepository: PredictionQueryRepository
) : PredictionRepository {
    override fun getPredictionList(
        page: Int,
        size: Int,
        sort: String
    ): PageResponse<Prediction> {
        val predictionExposedList = when (sort) {
            "upward" -> predictionQueryRepository.findALlPredictionBySentimentScore(
                page,
                size,
                false
            )
            "downward" -> predictionQueryRepository.findALlPredictionBySentimentScore(
                page,
                size,
                true
            )
            else -> predictionQueryRepository.findALlPredictionByPopular(page, size)
        }
        return PageResponse(predictionExposedList.content.map { it ->
            toDomain(it)
        }.toList(), page, size, predictionExposedList.hasNext)
    }

    override fun getPredictionDetail(tickerId: UUID): PredictionDetailQueryDto {
        return  predictionQueryRepository.findPredictionWithPriceInfoById(tickerId)
    }
}