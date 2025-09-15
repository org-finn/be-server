package finn.mapper

import finn.queryDto.TickerSearchQueryDto
import finn.response.search.TickerSearchPreviewListResponse

fun toDto(tickerDto: List<TickerSearchQueryDto>): TickerSearchPreviewListResponse {
    val tickerList = tickerDto.map { it ->
        TickerSearchPreviewListResponse.TickerSearchPreviewResponse(
            it.tickerId(), it.tickerCode(), it.shortCompanyName(),
            it.fullCompanyName()
        )
    }.toList()
    return TickerSearchPreviewListResponse(tickerList)
}