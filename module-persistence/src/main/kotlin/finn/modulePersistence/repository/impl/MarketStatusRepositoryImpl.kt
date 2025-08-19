package finn.modulePersistence.repository.impl

import finn.moduleDomain.entity.MarketStatus
import finn.moduleDomain.repository.MarketStatusRepository
import finn.modulePersistence.repository.exposed.MarketStatusExposedRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class MarketStatusRepositoryImpl(
    private val marketStatusExposedRepository: MarketStatusExposedRepository
) : MarketStatusRepository {
    override fun getOptionalMarketStatus(today: LocalDate): MarketStatus? {
        TODO("Not yet implemented")
    }
}