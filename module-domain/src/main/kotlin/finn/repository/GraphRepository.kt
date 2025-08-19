package finn.repository

import finn.queryDto.TickerGraphQueryDto
import java.time.LocalDate
import java.util.*

interface GraphRepository {

    fun getTickerGraph(
        tickerId: UUID,
        startDate: LocalDate,
        endDate: LocalDate,
        interval: Int,
        minimumCount: Long
    ): List<TickerGraphQueryDto>
}