package finn.repository.impl

import finn.entity.query.Ticker
import finn.mapper.toDomain
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
}