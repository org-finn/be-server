package finn.mapper

import finn.entity.ExchangeRate
import finn.entity.ExchangeRateExposed

fun toDomain(exchangeRateExposed: ExchangeRateExposed): ExchangeRate {
    return ExchangeRate.create(
        exchangeRateExposed.date.toLocalDate(),
        exchangeRateExposed.indexCode,
        exchangeRateExposed.indexInfo,
        exchangeRateExposed.value,
        exchangeRateExposed.changeRate
    )
}