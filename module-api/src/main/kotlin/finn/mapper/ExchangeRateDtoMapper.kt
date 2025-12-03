package finn.mapper

import finn.entity.ExchangeRate
import finn.response.exchangeRate.ExchangeRateRealTimeResponse

class ExchangeRateDtoMapper {

    companion object {
        fun toDto(exchangeRate: ExchangeRate): ExchangeRateRealTimeResponse {
            return ExchangeRateRealTimeResponse(
                exchangeRate.toString(), exchangeRate.indexCode,
                exchangeRate.indexInfo, exchangeRate.value, exchangeRate.changeRate
            )
        }
    }
}