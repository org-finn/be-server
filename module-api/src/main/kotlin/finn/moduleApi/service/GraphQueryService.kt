package finn.moduleApi.service

import finn.moduleApi.queryDto.TickerGraphQueryDto
import finn.moduleApi.repository.GraphRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class GraphQueryService(private val graphRepository: GraphRepository) {

    fun getTickerGraphData(tickerId: UUID, period: String): List<TickerGraphQueryDto> {
        return getTickerGraphData(tickerId, period)
    }
}