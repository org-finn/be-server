package finn.moduleApi.service

import finn.moduleDomain.queryDto.TickerGraphQueryDto
import finn.moduleDomain.repository.GraphRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class GraphQueryService(private val graphRepository: GraphRepository) {

    fun getTickerGraphData(tickerId: UUID, period: String): List<TickerGraphQueryDto> {
        return getTickerGraphData(tickerId, period)
    }
}