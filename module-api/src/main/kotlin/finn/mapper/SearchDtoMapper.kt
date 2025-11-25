package finn.mapper

import finn.queryDto.TickerQueryDto
import finn.response.search.TickerSearchPreviewListResponse

class SearchDtoMapper {
    companion object {
        fun toDto(tickerDto: List<TickerQueryDto>): TickerSearchPreviewListResponse {
            val tickerList = tickerDto.map {
                TickerSearchPreviewListResponse.TickerSearchPreviewResponse(
                    it.tickerId, it.tickerCode, it.shortCompanyName,
                    it.fullCompanyName
                )
            }.toList()
            return TickerSearchPreviewListResponse(tickerList)
        }
    }
}
