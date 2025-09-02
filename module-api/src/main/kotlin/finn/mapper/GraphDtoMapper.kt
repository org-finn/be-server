package finn.mapper

import finn.queryDto.TickerGraphQueryDto
import finn.queryDto.TickerRealTimeGraphQueryDto
import finn.response.graph.TickerGraphResponse
import finn.response.graph.TickerRealTimeGraphListResponse


fun toDto(period: String, graphDto: List<TickerGraphQueryDto>): TickerGraphResponse {
    val graphData = graphDto.map { it ->
        TickerGraphResponse.TickerGraphDataResponse(
            it.date().toString(),
            it.price(),
            it.changeRate(),
            it.positiveArticleCount(),
            it.negativeArticleCount(),
        )
    }.toList()
    return TickerGraphResponse(period, graphData)
}

fun toDto(dto: TickerRealTimeGraphQueryDto): TickerRealTimeGraphListResponse {
    val priceDataList = dto.priceDataList().map {
        TickerRealTimeGraphListResponse.TickerRealTimeGraphResponse(
            it.price(),
            it.hours(),
            it.index()
        )
    }.toList()

    return TickerRealTimeGraphListResponse(
        dto.priceDate(), dto.tickerId(),
        priceDataList, dto.maxLen()
    )
}
