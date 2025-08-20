package finn.repository.impl

import finn.queryDto.TickerGraphQueryDto
import finn.repository.GraphRepository
import finn.repository.query.GraphQueryRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
class GraphRepositoryImpl(
    private val graphQueryRepository: GraphQueryRepository
) : GraphRepository {
    override fun getTickerGraph(
        tickerId: UUID,
        startDate: LocalDate,
        endDate: LocalDate,
        interval: Int,
        minimumCount: Long
    ): List<TickerGraphQueryDto> {
        return graphQueryRepository.findByInterval(
            tickerId,
            startDate,
            endDate,
            interval,
            minimumCount
        )
    }
}