package finn.modulePersistence.repository.facade

import finn.moduleDomain.queryDto.TickerSearchQueryDto
import finn.moduleDomain.repository.TickerRepository
import finn.modulePersistence.repository.db.TickerExposedRepository
import org.springframework.stereotype.Repository

@Repository
class TickerRepositoryImpl(
    private val tickerExposedRepository: TickerExposedRepository
) : TickerRepository {
    override fun getTickerListBySearchKeyword(keyword: String): List<TickerSearchQueryDto> {
        TODO("Not yet implemented")
    }
}