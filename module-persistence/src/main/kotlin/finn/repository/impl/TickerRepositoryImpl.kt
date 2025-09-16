package finn.repository.impl

import finn.entity.query.Ticker
import finn.mapper.toDomain
import finn.queryDto.TickerSearchQueryDto
import finn.repository.TickerRepository
import finn.repository.exposed.TickerExposedRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository

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
    override fun findAll(): List<TickerSearchQueryDto> {
        return tickerExposedRepository.findAll()
    }
}