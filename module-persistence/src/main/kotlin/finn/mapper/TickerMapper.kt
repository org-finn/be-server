package finn.mapper

import finn.entity.TickerExposed
import finn.entity.query.Ticker

fun toDomain(ticker: TickerExposed): Ticker {
    return Ticker.create(
        ticker.id.value,
        ticker.code,
        ticker.shortCompanyName,
        ticker.fullCompanyName
    )
}