package finn.entity.dynamodb

import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import java.util.*

data class TickerPriceRealTimeEntity(
    val tickerId: UUID,
    val timeKey: String,   // 분 단위 타임스탬프 (SK)
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
) {
    // SDK v2용 Map 변환 메서드 (저장 시 사용)
    fun toItemMap(): Map<String, AttributeValue> {
        return mapOf(
            "PK" to AttributeValue.builder().s("ticker#$tickerId").build(),
            "SK" to AttributeValue.builder().s(timeKey).build(),
            "o" to AttributeValue.builder().n(open.toString()).build(),
            "h" to AttributeValue.builder().n(high.toString()).build(),
            "l" to AttributeValue.builder().n(low.toString()).build(),
            "c" to AttributeValue.builder().n(close.toString()).build(),
            "v" to AttributeValue.builder().n(volume.toString()).build()
        )
    }
}