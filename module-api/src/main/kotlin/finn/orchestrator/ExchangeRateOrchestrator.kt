package finn.orchestrator

import finn.mapper.ExchangeRateDtoMapper.Companion.toDto
import finn.response.exchangeRate.ExchangeRateRealTimeResponse
import finn.service.ExchangeRateService
import finn.transaction.ExposedTransactional
import org.springframework.stereotype.Service

@Service
@ExposedTransactional(readOnly = true)
class ExchangeRateOrchestrator(
    private val exchangeRateService: ExchangeRateService
) {

    fun getExchangeRateRealTime(indexCode: String): ExchangeRateRealTimeResponse {
        val exchangeRate = exchangeRateService.getExchangeRateRealTime(indexCode)
        return toDto(exchangeRate)
    }
}