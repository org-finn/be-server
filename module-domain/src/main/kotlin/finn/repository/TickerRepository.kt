package finn.repository

import finn.queryDto.TickerSearchQueryDto

interface TickerRepository {

    fun getTickerListBySearchKeyword(keyword: String): List<TickerSearchQueryDto>
}