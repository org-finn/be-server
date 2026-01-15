package finn.repository

import finn.entity.query.Ticker
import finn.queryDto.TickerQueryDto
import java.math.BigDecimal
import java.util.*

interface TickerRepository {

    fun getTickerByTickerCode(tickerCode: String): Ticker

    fun findAll(): List<TickerQueryDto>

    suspend fun getPreviousAtrByTickerId(tickerId: UUID): BigDecimal

    suspend fun updateTodayAtr(tickerId: UUID, todayAtr: BigDecimal)

    suspend fun updateAtrs(updates: Map<UUID, BigDecimal>)

    suspend fun getPreviousAtrsByIds(tickerIds: List<UUID>) : Map<UUID, BigDecimal>
}