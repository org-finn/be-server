package finn.modulePersistence.repository.facade

import finn.moduleDomain.entity.MarketStatus
import finn.moduleDomain.repository.MarketStatusRepository
import finn.modulePersistence.repository.db.MarketStatusExposedRepository
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