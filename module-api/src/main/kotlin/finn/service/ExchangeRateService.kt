package finn.service

import finn.entity.ExchangeRate
import finn.repository.ExchangeRateRepository
import org.springframework.stereotype.Service

@Service
class ExchangeRateService(
    private val exchangeRateRepository: ExchangeRateRepository
) {
    fun getExchangeRateRealTime(indexCode: String): ExchangeRate {
        return exchangeRateRepository.findByIndexInfo(indexCode)
    }
}