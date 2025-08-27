package finn.repository.impl

import finn.entity.query.MarketStatus
import finn.mapper.toDomain
import finn.repository.MarketStatusRepository
import finn.repository.query.MarketStatusQueryRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class MarketStatusRepositoryImpl(
    private val marketStatusQueryRepository: MarketStatusQueryRepository
) : MarketStatusRepository {
    override fun getOptionalMarketStatus(today: LocalDate): MarketStatus? {
        return marketStatusQueryRepository.findMarketStatusByDate(today)?.let {
            toDomain(it)
        }
    }
}