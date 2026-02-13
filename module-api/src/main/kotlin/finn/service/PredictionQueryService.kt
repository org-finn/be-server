package finn.service

import finn.entity.query.MarketStatus
import finn.entity.query.PredictionQ
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

    fun getPredictionList(
        pageRequest: PredictionPageRequest,
        userId: UUID?
    ): PageResponse<PredictionQueryDto> {
        val marketStatus =
            marketStatusRepository.getOptionalMarketStatus(LocalDate.now(clock))
        val isOpened =
            MarketStatus.checkIsOpened(marketStatus, clock)

        return predictionRepository.getPredictionListWithGraph(
            pageRequest.page,
            pageRequest.size,
            pageRequest.sort,
            isOpened,
            userId
        )
    }

    fun getPredictionDetail(tickerId: UUID): PredictionDetailQueryDto {
        return predictionRepository.getPredictionDetail(tickerId)
    }

    suspend fun getRecentSentimentScores(tickerId: UUID): List<Int> {
        return predictionRepository.getRecentSentimentScoreList(tickerId)
    }


    suspend fun findAllByTickerIdsForPrediction(tickerIds: List<UUID>): List<PredictionQ> {
        return predictionRepository.findAllByTickerIds(tickerIds)
    }

    suspend fun findAllForPrediction(): List<PredictionQ> {
        return predictionRepository.findAll()
    }

    suspend fun findYesterdayVolatilityMap(tickerIds: List<UUID>): Map<UUID, BigDecimal> {
        return predictionRepository.findYesterdayVolatilityMap(tickerIds)
    }
}