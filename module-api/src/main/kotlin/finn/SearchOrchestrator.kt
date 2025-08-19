package finn.orchestrator

import finn.mapper.toDto
import finn.response.search.TickerSearchPreviewListResponse
import finn.service.TickerQueryService
import finn.transaction.Transactional
import finn.validator.checkKeywordValid
import org.springframework.stereotype.Service

@Service
@Transactional(readOnly = true)
class SearchOrchestrator(
    private val tickerQueryService: TickerQueryService
) {

    fun getTickerSearchPreviewList(keyword: String?): TickerSearchPreviewListResponse {
        checkKeywordValid(keyword)
        val tickerDto = tickerQueryService.getTickerSearchList(keyword!!)
        return toDto(tickerDto)
    }
}