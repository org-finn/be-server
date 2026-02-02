package finn.repository

import finn.queryDto.TickerGraphQueryDto
import finn.queryDto.TickerRealTimeHistoryGraphQueryDto
import java.time.LocalDate
import java.util.*

interface GraphRepository {

    fun getTickerGraph(
        tickerId: UUID,
        startDate: LocalDate,
        endDate: LocalDate,
        interval: Int
    ): List<TickerGraphQueryDto>

    fun getRealTimeHistoryTickerGraph(
        tickerId: UUID,
        gte: Int?,
        missing: List<Int>?
    ): TickerRealTimeHistoryGraphQueryDto

    fun saveRealTimeTickerPrice(
        tickerId: UUID,
        time: String,
        open: Double,
        high: Double,
        low: Double,
        close: Double,
        volume: Long
    )
}