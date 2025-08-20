package finn.repository.impl

import finn.exception.CriticalDataPollutedException
import finn.paging.PageResponse
import finn.queryDto.PredictionDetailQueryDto
import finn.queryDto.PredictionQueryDto
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
    ): PageResponse<PredictionQueryDto> {
        val predictionExposedList = when (sort) {
            "popular" -> predictionQueryRepository.findALlPredictionByPopular(
                page,
                size
            )

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

            else -> throw CriticalDataPollutedException("Sort: $sort, 지원하지 않는 옵션입니다.")
        }
        return PageResponse(predictionExposedList.content, page, size, predictionExposedList.hasNext)
    }

    override fun getPredictionDetail(tickerId: UUID): PredictionDetailQueryDto {
        return predictionQueryRepository.findPredictionWithPriceInfoById(tickerId)
    }
}