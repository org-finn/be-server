package finn.moduleApi.service

import finn.moduleApi.queryDto.TickerSearchQueryDto
import finn.moduleApi.repository.TickerRepository
import org.springframework.stereotype.Service

@Service
class TickerQueryService(
    private val tickerRepository: TickerRepository
) {

    fun getTickerSearchList(keyword: String) : List<TickerSearchQueryDto> {
        return tickerRepository.getTickerListBySearchKeyword(keyword)
    }
}