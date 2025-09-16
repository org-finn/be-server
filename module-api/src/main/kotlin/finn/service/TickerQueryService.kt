package finn.service

import finn.entity.query.Ticker
import finn.queryDto.TickerSearchQueryDto
import finn.repository.TickerRepository
import org.springframework.stereotype.Service

@Service
class TickerQueryService(
    private val tickerRepository: TickerRepository
) {

    fun getTickerSearchList(keyword: String): List<TickerSearchQueryDto> {
        val tickerList = tickerRepository.findAll()
        return tickerList.filter {
            it.shortCompanyName().startsWith(keyword, ignoreCase = true) ||
                    it.shortCompanyNameKr().startsWith(keyword, ignoreCase = true)
        }
    }

    fun getTickerByTickerCode(tickerCode: String): Ticker {
        return tickerRepository.getTickerByTickerCode(tickerCode)
    }
}