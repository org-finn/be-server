package finn.repository

import finn.entity.Ticker
import finn.queryDto.TickerSearchQueryDto

interface TickerRepository {

    fun getTickerListBySearchKeyword(keyword: String): List<TickerSearchQueryDto>

    fun getTickerByTickerCode(tickerCode: String): Ticker
}