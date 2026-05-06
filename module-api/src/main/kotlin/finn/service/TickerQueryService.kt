package finn.service

import finn.filter.TickerSearchFilter
import finn.paging.PageResponse
import finn.queryDto.TickerJoinQueryDto
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

    fun getTickerSearchList(keyword: String, limit: Int? = null): List<TickerQueryDto> {
        val tickerList = tickerRepository.findAll()
        val filteredList = tickerSearchFilter.filterByKeyword(tickerList, keyword)
        return if (limit != null) filteredList.take(limit) else filteredList
    }

    fun getAllTickerList(): List<TickerQueryDto> {
        return tickerRepository.findAll()
    }

    fun getTickerListForJoin(page: Int, keyword: String?): PageResponse<TickerJoinQueryDto> {
        return tickerRepository.findAllByPageAndKeyword(page, keyword)
    }

    suspend fun findYesterdayAtrMap(tickerIds: List<UUID>): Map<UUID, BigDecimal> {
        return tickerRepository.getPreviousAtrsByIds(tickerIds)
    }
}
