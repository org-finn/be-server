package finn.mapper

import finn.entity.MarketStatus
import finn.entity.MarketStatusExposed

fun toDomain(marketStatus: MarketStatusExposed): MarketStatus {
    return MarketStatus.create(
        marketStatus.date, marketStatus.tradingHours,
        marketStatus.eventName
    )
}