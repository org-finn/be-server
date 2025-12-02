package finn.service

import finn.entity.TickerScore
import finn.entity.query.MarketStatus
import finn.exception.DomainPolicyViolationException
import finn.paging.PageResponse
import finn.paging.PredictionPageRequest
import finn.queryDto.PredictionDetailQueryDto
import finn.queryDto.PredictionQueryDto
import finn.repository.MarketStatusRepository
import finn.repository.PredictionRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Clock
import java.time.LocalDate
import java.util.*

@Service
class PredictionQueryService(
    private val predictionRepository: PredictionRepository,
    private val marketStatusRepository: MarketStatusRepository,
    private val clock: Clock
) {

    fun getPredictionList(pageRequest: PredictionPageRequest): PageResponse<PredictionQueryDto> {
        return when (pageRequest.param) {
            "keyword" -> predictionRepository.getPredictionListWithKeyword(
                pageRequest.page,
                pageRequest.size,
                pageRequest.sort
            )

            "article" -> predictionRepository.getPredictionListWithArticle(
                pageRequest.page,
                pageRequest.size,
                pageRequest.sort
            )

            "graph" -> {
                val marketStatus =
                    marketStatusRepository.getOptionalMarketStatus(LocalDate.now(clock))
                val isOpened =
                    MarketStatus.checkIsOpened(marketStatus, clock)

                predictionRepository.getPredictionListWithGraph(
                    pageRequest.page,
                    pageRequest.size,
                    pageRequest.sort,
                    isOpened
                )
            }

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