package finn.moduleApi.orchestrator

import finn.moduleApi.mapper.SearchDtoMapper
import finn.moduleApi.response.search.TickerSearchPreviewListResponse
import finn.moduleApi.service.TickerQueryService
import finn.moduleCommon.transaction.Transactional
import finn.moduleDomain.validator.SearchKeywordMatcher
import org.springframework.stereotype.Service

@Service
@Transactional(readOnly = true)
class SearchOrchestrator(
    private val tickerQueryService: TickerQueryService
) {

    fun getTickerSearchPreviewList(keyword: String?): TickerSearchPreviewListResponse {
        SearchKeywordMatcher.checkKeywordValid(keyword)
        val tickerDto = tickerQueryService.getTickerSearchList(keyword!!)
        return SearchDtoMapper.toDto(tickerDto)
    }
}