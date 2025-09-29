package finn.repository

import finn.entity.query.Ticker
import finn.queryDto.TickerQueryDto

interface TickerRepository {

    fun getTickerByTickerCode(tickerCode: String): Ticker

    fun findAll(): List<TickerQueryDto>
}