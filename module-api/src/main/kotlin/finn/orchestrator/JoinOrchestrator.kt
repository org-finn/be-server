package finn.orchestrator

import finn.mapper.TickerDtoMapper.Companion.toDto
import finn.response.userinfo.JoinTickerResponse
import finn.service.TickerQueryService
import finn.transaction.ExposedTransactional
import org.springframework.stereotype.Service

@Service
@ExposedTransactional(readOnly = true)
class JoinOrchestrator(
    private val tickerQueryService: TickerQueryService
) {
    fun getTickerList(page: Int): JoinTickerResponse {
        return toDto(tickerQueryService.getTickerListForJoin(page))
    }
}