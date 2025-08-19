package finn.mapper

import finn.queryDto.TickerGraphQueryDto
import finn.response.graph.TickerGraphResponse


fun toDto(period: String, graphDto: List<TickerGraphQueryDto>): TickerGraphResponse {
    val graphData = graphDto.map { it ->
        TickerGraphResponse.TickerGraphDataResponse(
            it.date().toString(),
            it.price(),
            it.changeRate()
        )
    }.toList()
    return TickerGraphResponse(period, graphData)
}
