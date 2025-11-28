package finn.service

import finn.entity.TickerScore
import finn.exception.DomainPolicyViolationException
import finn.paging.PageResponse
import finn.paging.PredictionPageRequest
import finn.queryDto.PredictionDetailQueryDto
import finn.queryDto.PredictionQueryDto
import finn.repository.PredictionRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*

@Service
class PredictionQueryService(private val predictionRepository: PredictionRepository) {

    fun getPredictionList(pageRequest: PredictionPageRequest): PageResponse<PredictionQueryDto> {
        return when (pageRequest.param) {
            "keyword" -> predictionRepository.getPredictionListWithArticle(
                pageRequest.page,
                pageRequest.size,
                pageRequest.sort
            )

            "article" -> predictionRepository.getPredictionListWithArticle(
                pageRequest.page,
                pageRequest.size,
                pageRequest.sort
            )

            "graph" -> predictionRepository.getPredictionListWithGraph(
                pageRequest.page,
                pageRequest.size,
                pageRequest.sort
            )

            null -> predictionRepository.getPredictionListDefault(
                pageRequest.page,
                pageRequest.size,
                pageRequest.sort
            )

            else -> throw DomainPolicyViolationException("지원하지 않는 param 옵셥입니다.")
        }
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

    suspend fun getYesterdayVolatility(tickerId: UUID): BigDecimal {
        return predictionRepository.getYesterdayVolatilityByTickerId(tickerId)
    }
}