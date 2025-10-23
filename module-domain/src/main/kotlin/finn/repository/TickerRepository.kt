package finn.repository

import finn.entity.query.Ticker
import finn.queryDto.TickerQueryDto
import java.util.*

interface TickerRepository {

    fun getTickerByTickerCode(tickerCode: String): Ticker

    fun findAll(): List<TickerQueryDto>

    suspend fun getPreviousAtrByTickerId(tickerId: UUID): Double
}