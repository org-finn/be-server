package finn.entity.dynamodb

import java.time.LocalDateTime

data class TickerPriceRealTimeEntity(
    val startTime: LocalDateTime,
    val close: Double,
) {
}