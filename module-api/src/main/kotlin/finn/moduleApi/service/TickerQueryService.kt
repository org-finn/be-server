package finn.moduleApi.service

import finn.moduleDomain.queryDto.TickerSearchQueryDto
import finn.moduleDomain.repository.TickerRepository
import org.springframework.stereotype.Service

@Service
class TickerQueryService(
    private val tickerRepository: TickerRepository
) {

    fun getTickerSearchList(keyword: String) : List<TickerSearchQueryDto> {
        return tickerRepository.getTickerListBySearchKeyword(keyword)
    }
}