package finn.repository

import finn.entity.query.Ticker
import finn.paging.PageResponse
import finn.queryDto.TickerCodeQueryDto
import finn.queryDto.TickerJoinQueryDto
import finn.queryDto.TickerQueryDto
import java.math.BigDecimal
import java.util.*

interface TickerRepository {

    fun getTickerByTickerCode(tickerCode: String): Ticker

    fun getTickerIdByTickerCode(tickerCode: String): UUID

    fun findAll(): List<TickerQueryDto>

    fun findAllByPageAndKeyword(page: Int, keyword: String?): PageResponse<TickerJoinQueryDto>

    suspend fun getPreviousAtrByTickerId(tickerId: UUID): BigDecimal

    suspend fun updateTodayAtr(tickerId: UUID, todayAtr: BigDecimal)

    suspend fun updateAtrs(updates: Map<UUID, BigDecimal>)

    suspend fun getPreviousAtrsByIds(tickerIds: List<UUID>): Map<UUID, BigDecimal>

    fun validTickersByTickerCode(tickerCodes: List<String>)

    fun findAllCode(): TickerCodeQueryDto
}