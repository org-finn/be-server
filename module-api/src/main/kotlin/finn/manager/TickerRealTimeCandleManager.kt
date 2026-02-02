package finn.manager

import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max
import kotlin.math.min

@Component
class TickerRealTimeCandleManager {

    // Key: 종목코드, Value: 현재 진행 중인 1분봉 데이터
    private val currentCandles = ConcurrentHashMap<String, CandleData>()

    /**
     * 1. 실시간 가격 업데이트 (WebSocket Handler에서 호출)
     * - 현재가가 들어올 때마다 High, Low, Close, Volume 갱신
     */
    fun updatePrice(tickerId: UUID, price: Double, volume: Long) {
        currentCandles.compute(tickerId.toString()) { _, existingCandle ->
            if (existingCandle == null) {
                // 해당 분의 첫 데이터면 새로 생성 (Open = Close = High = Low = 현재가)
                CandleData(
                    open = price,
                    high = price,
                    low = price,
                    close = price,
                    volume = volume
                )
            } else {
                // 이미 있으면 갱신 (Synchronized 불필요, compute 자체가 원자적 연산 보장 시도)
                // 단, 더 확실한 정합성을 위해 객체 내부 수정만 수행
                synchronized(existingCandle) {
                    existingCandle.high = max(existingCandle.high, price)
                    existingCandle.low = min(existingCandle.low, price)
                    existingCandle.close = price
                    existingCandle.volume += volume // 체결량 누적
                }
                existingCandle
            }
        }
    }

    /**
     * 2. 완성된 캔들 꺼내기 (DB 저장 스케줄러에서 호출)
     * - 호출 시 현재 저장된 데이터를 반환하고 맵에서 '삭제'함
     * - 삭제된 직후 들어오는 데이터는 새로운 1분봉의 시작이 됨
     */
    fun popAllCandles(): Map<String, CandleData> {
        val snapshot = HashMap<String, CandleData>()

        // 현재 맵에 있는 모든 키를 순회하며 데이터를 꺼냄 (Pop)
        currentCandles.keys.forEach { code ->
            // remove(code)는 Value를 반환함. 원자적 연산.
            val candle = currentCandles.remove(code)
            if (candle != null) {
                snapshot[code] = candle
            }
        }
        return snapshot
    }

    fun isEmpty(): Boolean {
        return currentCandles.isEmpty()
    }
}

// 메모리에 저장될 1분봉 데이터 모델
data class CandleData(
    var open: Double,
    var high: Double,
    var low: Double,
    var close: Double,
    var volume: Long,
    var startTime: LocalDateTime = LocalDateTime.now()
)