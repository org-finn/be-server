package finn.queryDto

import java.util.*

data class TickerJoinQueryDto(
    val tickerId: UUID,
    val tickerCode: String,
    val shortCompanyName: String,
    val predictionStrategy: String,
    val sentiment: Int,
    var graphData: GraphDataQueryDto?
)
