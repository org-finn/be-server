package finn.moduleDomain.repository

import finn.moduleDomain.queryDto.TickerSearchQueryDto

interface TickerRepository {

    fun getTickerListBySearchKeyword(keyword: String): List<TickerSearchQueryDto>
}