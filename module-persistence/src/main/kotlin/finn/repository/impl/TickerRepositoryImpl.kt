package finn.repository.impl

import finn.cache.CacheUtil
import finn.entity.query.Ticker
import finn.mapper.toDomain
import finn.queryDto.TickerSearchQueryDto
import finn.repository.TickerRepository
import finn.repository.exposed.TickerExposedRepository
import org.springframework.stereotype.Repository

@Repository
class TickerRepositoryImpl(
    private val tickerExposedRepository: TickerExposedRepository,
    private val cacheUtil: CacheUtil
) : TickerRepository {

    companion object {
        private const val TICKER_LIST_CACHE_KEY = "tickers:all"
    }


    override fun getTickerListBySearchKeyword(keyword: String): List<TickerSearchQueryDto> {
        val tickerList = findAll()
        return tickerList.filter {
            it.shortCompanyName().startsWith(keyword, ignoreCase = true) ||
                    it.shortCompanyNameKr().startsWith(keyword, ignoreCase = true)
        }
    }

    override fun getTickerByTickerCode(tickerCode: String): Ticker {
        return toDomain(tickerExposedRepository.findByTickerCode(tickerCode))
    }

    private fun findAll(): List<TickerSearchQueryDto> {
        val cachedData = cacheUtil.get(TICKER_LIST_CACHE_KEY, List::class.java)

        if (cachedData != null) {
            @Suppress("UNCHECKED_CAST")
            return cachedData as List<TickerSearchQueryDto>
        }

        val tickersFromDb = tickerExposedRepository.findAll()
        cacheUtil.set(TICKER_LIST_CACHE_KEY, tickersFromDb, 86400L)
        return tickersFromDb
    }
}