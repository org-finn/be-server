package finn.orchestrator

import finn.mapper.SearchDtoMapper
import finn.response.search.TickerSearchPreviewListResponse
import finn.service.TickerQueryService
import finn.transaction.ExposedTransactional
import finn.validator.checkKeywordValid
import org.springframework.stereotype.Service

@Service
@ExposedTransactional(readOnly = true)
class SearchOrchestrator(
    private val tickerQueryService: TickerQueryService
) {

    fun getTickerSearchPreviewList(keyword: String?): TickerSearchPreviewListResponse {
        checkKeywordValid(keyword)
        val tickerDto = tickerQueryService.getTickerSearchList(keyword!!)
        return SearchDtoMapper.toDto(tickerDto)
    }
}