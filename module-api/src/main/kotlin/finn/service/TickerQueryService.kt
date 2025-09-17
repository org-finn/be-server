package finn.service

import finn.entity.query.Ticker
import finn.filter.TickerSearchFilter
import finn.queryDto.TickerSearchQueryDto
import finn.repository.TickerRepository
import org.springframework.stereotype.Service

@Service
class TickerQueryService(
    private val tickerRepository: TickerRepository,
    private val tickerSearchFilter: TickerSearchFilter
) {

    fun getTickerSearchList(keyword: String): List<TickerSearchQueryDto> {
        val tickerList = tickerRepository.findAll()
        return tickerSearchFilter.filterByKeyword(tickerList, keyword)
    }

    fun getTickerByTickerCode(tickerCode: String): Ticker {
        return tickerRepository.getTickerByTickerCode(tickerCode)
    }
}