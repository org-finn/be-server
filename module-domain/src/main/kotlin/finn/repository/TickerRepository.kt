package finn.repository

import finn.entity.query.Ticker
import finn.queryDto.TickerSearchQueryDto

interface TickerRepository {

    fun getTickerByTickerCode(tickerCode: String): Ticker

    fun findAll(): List<TickerSearchQueryDto>
}