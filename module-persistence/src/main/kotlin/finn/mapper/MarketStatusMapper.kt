package finn.mapper

import finn.entity.MarketStatusExposed
import finn.entity.query.MarketStatus

fun toDomain(marketStatus: MarketStatusExposed): MarketStatus {
    return MarketStatus.create(
        marketStatus.date, marketStatus.tradingHours,
        marketStatus.eventName
    )
}