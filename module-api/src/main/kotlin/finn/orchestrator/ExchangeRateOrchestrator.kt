package finn.orchestrator

import finn.response.exchangeRate.ExchangeRateRealTimeResponse
import finn.transaction.ExposedTransactional
import org.springframework.stereotype.Service

@Service
@ExposedTransactional(readOnly = true)
class ExchangeRateOrchestrator {

    fun getExchangeRateRealTime(): ExchangeRateRealTimeResponse {

    }
}