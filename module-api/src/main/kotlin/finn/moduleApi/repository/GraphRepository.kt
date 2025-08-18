package finn.moduleApi.repository

import finn.moduleApi.queryDto.TickerGraphQueryDto
import java.util.*

interface GraphRepository {

    fun getTickerGraph(tickerId: UUID, period: String): List<TickerGraphQueryDto>
}