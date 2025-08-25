package finn.repository.impl

import finn.exception.CriticalDataPollutedException
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
        interval: Int
    ): List<TickerGraphQueryDto> {
        return when (interval) {
            1 -> graphQueryRepository.findDaily(
                tickerId,
                startDate,
                endDate
            )

            7 -> graphQueryRepository.findByInterval(
                tickerId,
                startDate,
                endDate,
                interval
            )

            else -> throw CriticalDataPollutedException("Interval: $interval, 지원하지 않는 주기입니다.")
        }
    }
}