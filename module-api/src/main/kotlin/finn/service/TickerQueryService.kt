package finn.service

import finn.entity.query.Ticker
import finn.queryDto.TickerSearchQueryDto
import finn.repository.TickerRepository
import org.springframework.stereotype.Service

@Service
class TickerQueryService(
    private val tickerRepository: TickerRepository
) {

    fun getTickerSearchList(keyword: String) : List<TickerSearchQueryDto> {
        return tickerRepository.getTickerListBySearchKeyword(keyword)
    }

    fun getTickerByTickerCode(tickerCode: String) : Ticker {
        return tickerRepository.getTickerByTickerCode(tickerCode)
    }
}