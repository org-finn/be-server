package finn.repository

import finn.queryDto.TickerGraphQueryDto
import finn.queryDto.TickerRealTimeGraphQueryDto
import java.time.LocalDate
import java.util.*

interface GraphRepository {

    fun getTickerGraph(
        tickerId: UUID,
        startDate: LocalDate,
        endDate: LocalDate,
        interval: Int
    ): List<TickerGraphQueryDto>

    fun getRealTimeTickerGraph(
        tickerId: UUID,
        gte: Int?,
        missing: List<Int>?
    ) : TickerRealTimeGraphQueryDto
}