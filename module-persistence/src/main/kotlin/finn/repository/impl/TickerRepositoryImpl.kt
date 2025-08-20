package finn.repository.impl

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
}