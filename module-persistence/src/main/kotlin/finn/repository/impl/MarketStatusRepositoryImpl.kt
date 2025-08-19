package finn.repository.impl

import finn.entity.MarketStatus
import finn.mapper.toDomain
import finn.repository.MarketStatusRepository
import finn.repository.exposed.MarketStatusExposedRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class MarketStatusRepositoryImpl(
    private val marketStatusExposedRepository: MarketStatusExposedRepository
) : MarketStatusRepository {
    override fun getOptionalMarketStatus(today: LocalDate): MarketStatus? {
        return marketStatusExposedRepository.findMarketStatusByDate(today)?.let {
            toDomain(it)
        }
    }
}