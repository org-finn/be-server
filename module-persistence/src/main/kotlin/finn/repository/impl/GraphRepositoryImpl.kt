package finn.repository.impl

import finn.exception.CriticalDataPollutedException
import finn.queryDto.TickerGraphQueryDto
import finn.queryDto.TickerRealTimeGraphQueryDto
import finn.repository.GraphRepository
import finn.repository.dynamodb.TickerPriceRealTimeDynamoDbRepository
import finn.repository.exposed.GraphExposedRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
class GraphRepositoryImpl(
    private val graphExposedRepository: GraphExposedRepository,
    private val tickerPriceRealTimeDynamoDbRepository: TickerPriceRealTimeDynamoDbRepository
) : GraphRepository {
    override fun getTickerGraph(
        tickerId: UUID,
        startDate: LocalDate,
        endDate: LocalDate,
        interval: Int
    ): List<TickerGraphQueryDto> {
        return when (interval) {
            1 -> graphExposedRepository.findDaily(
                tickerId,
                startDate,
                endDate
            )

            7 -> graphExposedRepository.findByInterval(
                tickerId,
                startDate,
                endDate,
                interval
            )

            else -> throw CriticalDataPollutedException("Interval: $interval, 지원하지 않는 주기입니다.")
        }
    }

    override fun getRealTimeTickerGraph(
        tickerId: UUID,
        gte: Int?,
        missing: List<Int>?
    ): TickerRealTimeGraphQueryDto {
        return tickerPriceRealTimeDynamoDbRepository.getLatestRealTimeData(tickerId, gte, missing)
    }
}