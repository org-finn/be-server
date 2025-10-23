package finn.service

import finn.entity.TickerScore
import finn.paging.PageResponse
import finn.paging.PredictionPageRequest
import finn.queryDto.PredictionDetailQueryDto
import finn.queryDto.PredictionQueryDto
import finn.repository.PredictionRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class PredictionQueryService(private val predictionRepository: PredictionRepository) {

    fun getPredictionList(pageRequest: PredictionPageRequest): PageResponse<PredictionQueryDto> {
        return predictionRepository.getPredictionList(
            pageRequest.page,
            pageRequest.size,
            pageRequest.sort
        )
    }

    fun getPredictionDetail(tickerId: UUID): PredictionDetailQueryDto {
        return predictionRepository.getPredictionDetail(tickerId)
    }

    suspend fun getRecentSentimentScores(tickerId: UUID): List<Int> {
        return predictionRepository.getRecentSentimentScoreList(tickerId)
    }

    suspend fun getTodaySentimentScore(tickerId: UUID): Int {
        return predictionRepository.getRecentScoreByTickerId(tickerId)
    }

    suspend fun getAllTickerTodaySentimentScore(): List<TickerScore> {
        return predictionRepository.getAllTickerRecentScore()
    }

    suspend fun getYesterdayVolatility(tickerId: UUID): Double {
        return predictionRepository.getYesterdayVolatilityByTickerId(tickerId)
    }
}