package finn.service

import finn.converter.getInterval
import finn.converter.getStartDate
import finn.queryDto.TickerGraphQueryDto
import finn.queryDto.TickerRealTimeHistoryGraphQueryDto
import finn.repository.GraphRepository
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDate
import java.util.*

@Service
class GraphQueryService(private val graphRepository: GraphRepository, private val clock: Clock) {

    fun getTickerGraphData(tickerId: UUID, period: String): List<TickerGraphQueryDto> {
        val interval = getInterval(period)
        val endDate = LocalDate.now(clock)
        val startDate = getStartDate(period, endDate)
        return graphRepository.getTickerGraph(tickerId, startDate, endDate, interval)
    }

    fun getTickerRealTimeGraphData(tickerId: UUID, gte: Int?, missing: List<Int>?) : TickerRealTimeHistoryGraphQueryDto {
        return graphRepository.getRealTimeHistoryTickerGraph(tickerId, gte, missing)
    }
}