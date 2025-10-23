package finn.service

import finn.entity.query.Ticker
import finn.filter.TickerSearchFilter
import finn.queryDto.TickerQueryDto
import finn.repository.TickerRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*

@Service
class TickerQueryService(
    private val tickerRepository: TickerRepository,
    private val tickerSearchFilter: TickerSearchFilter
) {

    fun getTickerSearchList(keyword: String): List<TickerQueryDto> {
        val tickerList = tickerRepository.findAll()
        return tickerSearchFilter.filterByKeyword(tickerList, keyword)
    }

    fun getAllTickerList() : List<TickerQueryDto> {
        return tickerRepository.findAll()
    }

    fun getTickerByTickerCode(tickerCode: String): Ticker {
        return tickerRepository.getTickerByTickerCode(tickerCode)
    }

    suspend fun getYesterdayAtr(tickerId: UUID): BigDecimal {
        return tickerRepository.getPreviousAtrByTickerId(tickerId)
    }
}