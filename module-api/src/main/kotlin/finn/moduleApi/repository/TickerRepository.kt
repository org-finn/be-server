package finn.moduleApi.repository

import finn.moduleApi.queryDto.TickerSearchQueryDto

interface TickerRepository {

    fun getTickerListBySearchKeyword(keyword: String): List<TickerSearchQueryDto>
}