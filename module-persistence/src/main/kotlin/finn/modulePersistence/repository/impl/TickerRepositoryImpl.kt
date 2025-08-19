package finn.modulePersistence.repository.impl

import finn.moduleDomain.queryDto.TickerSearchQueryDto
import finn.moduleDomain.repository.TickerRepository
import finn.modulePersistence.repository.exposed.TickerExposedRepository
import org.springframework.stereotype.Repository

@Repository
class TickerRepositoryImpl(
    private val tickerExposedRepository: TickerExposedRepository
) : TickerRepository {
    override fun getTickerListBySearchKeyword(keyword: String): List<TickerSearchQueryDto> {
        TODO("Not yet implemented")
    }
}