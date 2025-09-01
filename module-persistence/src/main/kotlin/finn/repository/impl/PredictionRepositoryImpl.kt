package finn.repository.impl

import finn.entity.command.PredictionC
import finn.entity.query.PredictionQ
import finn.exception.CriticalDataPollutedException
import finn.insertDto.PredictionToInsert
import finn.mapper.toDomain
import finn.paging.PageResponse
import finn.queryDto.PredictionDetailQueryDto
import finn.queryDto.PredictionQueryDto
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
    ): PageResponse<PredictionQueryDto> {
        val predictionExposedList = when (sort) {
            "popular" -> predictionExposedRepository.findALlPredictionByPopular(
                page,
                size
            )

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

            else -> throw CriticalDataPollutedException("Sort: $sort, 지원하지 않는 옵션입니다.")
        }
        return PageResponse(
            predictionExposedList.content,
            page,
            size,
            predictionExposedList.hasNext
        )
    }

    override fun getPredictionDetail(tickerId: UUID): PredictionDetailQueryDto {
        return predictionExposedRepository.findPredictionWithPriceInfoById(tickerId)
    }

    override fun getRecentSentimentScoreList(tickerId: UUID): List<Int> {
        return predictionExposedRepository.findTodaySentimentScoreByTickerId(tickerId)
    }

    override fun savePrediction(prediction: PredictionC) {
        val predictionToInsert = PredictionToInsert(
            prediction.tickerId,
            prediction.tickerCode,
            prediction.shortCompanyName,
            prediction.positiveArticleCount,
            prediction.negativeArticleCount,
            prediction.neutralArticleCount,
            prediction.sentimentScore,
            prediction.sentiment,
            prediction.predictionStrategy.strategy,
            prediction.predictionDate
        )
        predictionExposedRepository.save(predictionToInsert)
    }

    override fun updatePrediction(prediction: PredictionC): PredictionQ {
        val predictionToUpdate = PredictionToInsert(
            prediction.tickerId,
            prediction.tickerCode,
            prediction.shortCompanyName,
            prediction.positiveArticleCount,
            prediction.negativeArticleCount,
            prediction.neutralArticleCount,
            prediction.sentimentScore,
            prediction.sentiment,
            prediction.predictionStrategy.strategy,
            prediction.predictionDate
        )
        val updatedPrediction = predictionExposedRepository.update(predictionToUpdate)
        return toDomain(updatedPrediction)
    }
}