package finn.moduleApi.mapper

import finn.moduleApi.queryDto.TickerSearchQueryDto
import finn.moduleApi.response.search.TickerSearchPreviewListResponse
import finn.moduleApi.response.search.TickerSearchPreviewListResponse.TickerSearchPreviewResponse

object SearchDtoMapper {

    fun toDto(tickerDto: List<TickerSearchQueryDto>) : TickerSearchPreviewListResponse {
        val tickerList = tickerDto.map {
            it -> TickerSearchPreviewResponse(it.getTickerId(), it.getTickerCode(), it.getShortCompanyName(),
                it.getFullCompanyName())
        }.toList()
        return TickerSearchPreviewListResponse(tickerList)
    }
}