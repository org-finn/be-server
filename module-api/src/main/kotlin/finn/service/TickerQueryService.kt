package finn.service

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

    suspend fun findYesterdayAtrMap(tickerIds: List<UUID>): Map<UUID, BigDecimal> {
        return tickerRepository.getPreviousAtrsByIds(tickerIds)
    }
}