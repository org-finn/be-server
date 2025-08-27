package finn.repository

import finn.entity.query.Ticker
import finn.queryDto.TickerSearchQueryDto

interface TickerRepository {

    fun getTickerListBySearchKeyword(keyword: String): List<TickerSearchQueryDto>

    fun getTickerByTickerCode(tickerCode: String): Ticker
}