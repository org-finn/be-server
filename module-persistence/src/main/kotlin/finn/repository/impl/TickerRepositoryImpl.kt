package finn.repository.impl

import finn.entity.query.Ticker
import finn.mapper.toDomain
import finn.queryDto.TickerCodeQueryDto
import finn.queryDto.TickerQueryDto
import finn.repository.TickerRepository
import finn.repository.exposed.TickerExposedRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.*

@Repository
class TickerRepositoryImpl(
    private val tickerExposedRepository: TickerExposedRepository,
) : TickerRepository {

    companion object {
        private const val TICKER_LIST_CACHE_KEY = "'tickers:all'"
        private const val TICKER_CODE_LIST_CACHE_KEY = "'tickers:code:all'"
    }

    override fun getTickerByTickerCode(tickerCode: String): Ticker {
        return toDomain(tickerExposedRepository.findByTickerCode(tickerCode))
    }

    @Cacheable("tickerSearchList", key = TICKER_LIST_CACHE_KEY)
    override fun findAll(): List<TickerQueryDto> {
        return tickerExposedRepository.findAll()
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
    override fun findAllCode(): TickerCodeQueryDto {
        return tickerExposedRepository.findAllWithTickerCodeAndExchangeCode()
    }
}