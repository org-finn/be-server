package finn.moduleDomain.repository

import finn.moduleDomain.queryDto.TickerGraphQueryDto
import java.util.*

interface GraphRepository {

    fun getTickerGraph(tickerId: UUID, period: String): List<TickerGraphQueryDto>
}