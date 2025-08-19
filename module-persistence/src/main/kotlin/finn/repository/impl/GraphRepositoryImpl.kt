package finn.repository.impl

import finn.queryDto.TickerGraphQueryDto
import finn.repository.GraphRepository
import finn.repository.exposed.GraphExposedRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
class GraphRepositoryImpl(
    private val graphExposedRepository: GraphExposedRepository
) : GraphRepository {
    override fun getTickerGraph(
        tickerId: UUID,
        startDate: LocalDate,
        endDate: LocalDate,
        interval: Int,
        minimumCount: Long
    ): List<TickerGraphQueryDto> {
        return graphExposedRepository.findByInterval(
            tickerId,
            startDate,
            endDate,
            interval,
            minimumCount
        )
    }
}