package finn.mapper

import finn.queryDto.TickerGraphQueryDto
import finn.queryDto.TickerRealTimeHistoryGraphQueryDto
import finn.response.graph.RealTimeTickerPriceHistoryResponse
import finn.response.graph.TickerGraphResponse


fun toDto(period: String, graphDto: List<TickerGraphQueryDto>): TickerGraphResponse {
    val graphData = graphDto.map {
        TickerGraphResponse.TickerGraphDataResponse(
            it.date.toString(),
            it.price,
            it.changeRate,
            it.positiveArticleRatio,
            it.negativeArticleRatio,
        )
    }.toList()
    return TickerGraphResponse(period, graphData)
}

fun toDto(dto: TickerRealTimeHistoryGraphQueryDto): RealTimeTickerPriceHistoryResponse {
    val priceDataList = dto.priceDataList.map {
        RealTimeTickerPriceHistoryResponse.TickerRealTimeGraphResponse(
            it.price,
            it.hours,
            it.index
        )
    }.toList()

    return RealTimeTickerPriceHistoryResponse(
        dto.priceDate, dto.tickerId,
        priceDataList, dto.maxLen
    )
}
