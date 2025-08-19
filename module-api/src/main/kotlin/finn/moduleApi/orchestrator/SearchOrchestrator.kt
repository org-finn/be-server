package finn.moduleApi.orchestrator

import finn.moduleApi.mapper.toDto
import finn.moduleApi.response.search.TickerSearchPreviewListResponse
import finn.moduleApi.service.TickerQueryService
import finn.moduleCommon.transaction.Transactional
import finn.moduleDomain.validator.checkKeywordValid
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