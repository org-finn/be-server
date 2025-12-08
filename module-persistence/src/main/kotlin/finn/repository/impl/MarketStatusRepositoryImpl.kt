package finn.repository.impl

import finn.entity.query.MarketStatus
import finn.mapper.toDomain
import finn.repository.MarketStatusRepository
import finn.repository.exposed.MarketStatusExposedRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class MarketStatusRepositoryImpl(
    private val marketStatusExposedRepository: MarketStatusExposedRepository
) : MarketStatusRepository {

    companion object {
        private const val MARKET_STATUS_CACHE_KEY = "'marketStatus:today'"
    }

    @Cacheable("marketStatus", key=MARKET_STATUS_CACHE_KEY)
    override fun getOptionalMarketStatus(today: LocalDate): MarketStatus? {
        return marketStatusExposedRepository.findMarketStatusByDate(today)?.let {
            toDomain(it)
        }
    }
}