package finn.moduleApi.mapper

import finn.moduleApi.queryDto.TickerGraphQueryDto
import finn.moduleApi.response.graph.TickerGraphResponse
import finn.moduleApi.response.graph.TickerGraphResponse.TickerGraphDataResponse

object GraphDtoMapper {

    fun toDto(period: String, graphDto: List<TickerGraphQueryDto>): TickerGraphResponse {
        val graphData = graphDto.map { it ->
            TickerGraphDataResponse(it.getDate().toString(), it.getPrice(), it.getChangeRate())
        }.toList()
        return TickerGraphResponse(period, graphData)
    }
}