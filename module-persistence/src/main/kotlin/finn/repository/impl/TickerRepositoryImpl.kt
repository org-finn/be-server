package finn.repository.impl

import finn.entity.query.Ticker
import finn.mapper.toDomain
import finn.paging.PageResponse
import finn.queryDto.GraphDataQueryDto
import finn.queryDto.TickerCodeQueryDto
import finn.queryDto.TickerJoinQueryDto
import finn.queryDto.TickerQueryDto
import finn.repository.TickerRepository
import finn.repository.exposed.GraphExposedRepository
import finn.repository.exposed.TickerExposedRepository
import finn.transaction.ExposedTransactional
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.*

@Repository
class TickerRepositoryImpl(
    private val tickerExposedRepository: TickerExposedRepository,
    private val graphExposedRepository: GraphExposedRepository,
) : TickerRepository {

    companion object {
        private const val TICKER_LIST_CACHE_KEY = "'tickers:all'"
        private const val TICKER_ID_LIST_CACHE_KEY = "'tickers:id:all'"
        private const val TICKER_CODE_LIST_CACHE_KEY = "'tickers:code:all'"
    }

    override fun getTickerByTickerCode(tickerCode: String): Ticker {
        return toDomain(tickerExposedRepository.findByTickerCode(tickerCode))
    }

    @Cacheable("tickerIdList", key = TICKER_ID_LIST_CACHE_KEY)
    @ExposedTransactional(readOnly = true)
    override fun getTickerIdByTickerCode(tickerCode: String): UUID {
        return tickerExposedRepository.findTickerIdByTickerCode(tickerCode)
    }

    @Cacheable("tickerSearchList", key = TICKER_LIST_CACHE_KEY)
    override fun findAll(): List<TickerQueryDto> {
        return tickerExposedRepository.findAll()
    }

    override fun findAllByPage(page: Int): PageResponse<TickerJoinQueryDto> {
        val results = tickerExposedRepository.findAllByPage(page)
        setGraphData(results)
        return results
    }

    private fun setGraphData(results: PageResponse<TickerJoinQueryDto>) {
        val data = graphExposedRepository.findGraphDataForPredictionWhenClosed()
        results.content.forEach { dtoImpl ->
            val tickerId = dtoImpl.tickerId
            data[tickerId]?.let {
                val graphData = GraphDataQueryDto(false, it)
                dtoImpl.graphData = graphData
            }
        }
    }

    override suspend fun getPreviousAtrByTickerId(tickerId: UUID): BigDecimal {
        return tickerExposedRepository.findPreviousAtrByTickerId(tickerId)
    }

    override suspend fun updateTodayAtr(tickerId: UUID, todayAtr: BigDecimal) {
        tickerExposedRepository.updateTodayAtrByTickerId(tickerId, todayAtr)
    }

    override suspend fun updateAtrs(updates: Map<UUID, BigDecimal>) {
        tickerExposedRepository.batchUpdateAtr(updates)
    }

    override suspend fun getPreviousAtrsByIds(tickerIds: List<UUID>): Map<UUID, BigDecimal> {
        return tickerExposedRepository.findAtrsByIds(tickerIds)
    }

    override fun validTickersByTickerCode(tickerCodes: List<String>) {
        tickerExposedRepository.existTickersByTickerCode(tickerCodes)
    }

    @Cacheable("tickerCodeList", key = TICKER_CODE_LIST_CACHE_KEY)
    @ExposedTransactional(readOnly = true)
    override fun findAllCode(): TickerCodeQueryDto {
        return tickerExposedRepository.findAllWithTickerCodeAndExchangeCode()
    }
}