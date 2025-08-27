package finn.repository.impl

import finn.entity.query.Ticker
import finn.mapper.toDomain
import finn.queryDto.TickerSearchQueryDto
import finn.repository.TickerRepository
import finn.repository.exposed.TickerExposedRepository
import org.springframework.stereotype.Repository

@Repository
class TickerRepositoryImpl(
    private val tickerExposedRepository: TickerExposedRepository
) : TickerRepository {
    override fun getTickerListBySearchKeyword(keyword: String): List<TickerSearchQueryDto> {
        return tickerExposedRepository.findTickerListBySearchKeyword(keyword)
    }

    override fun getTickerByTickerCode(tickerCode: String): Ticker {
        return toDomain(tickerExposedRepository.findByTickerCode(tickerCode))
    }
}