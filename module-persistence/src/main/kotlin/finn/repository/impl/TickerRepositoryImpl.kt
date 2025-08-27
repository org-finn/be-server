package finn.repository.impl

import finn.entity.query.Ticker
import finn.mapper.toDomain
import finn.queryDto.TickerSearchQueryDto
import finn.repository.TickerRepository
import finn.repository.query.TickerQueryRepository
import org.springframework.stereotype.Repository

@Repository
class TickerRepositoryImpl(
    private val tickerQueryRepository: TickerQueryRepository
) : TickerRepository {
    override fun getTickerListBySearchKeyword(keyword: String): List<TickerSearchQueryDto> {
        return tickerQueryRepository.findTickerListBySearchKeyword(keyword)
    }

    override fun getTickerByTickerCode(tickerCode: String): Ticker {
        return toDomain(tickerQueryRepository.findByTickerCode(tickerCode))
    }
}