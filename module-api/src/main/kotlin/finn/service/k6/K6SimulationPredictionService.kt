package finn.service.k6

import kotlinx.coroutines.delay
import org.springframework.stereotype.Service
import java.util.concurrent.ThreadLocalRandom

@Service
class K6SimulationPredictionService() {
    // [설정] 읽기: 100ms ~ 200ms
    private val READ_MIN = 100L
    private val READ_MAX = 200L

    // [설정] 쓰기: 200ms ~ 350ms
    private val WRITE_MIN = 200L
    private val WRITE_MAX = 350L

    // 랜덤 지연 시간을 생성하는 헬퍼 함수
    private fun randomLatency(min: Long, max: Long): Long {
        return ThreadLocalRandom.current().nextLong(min, max + 1)
    }

    // ==========================================
    // 1. Blocking 메서드 (Thread.sleep)
    // ==========================================
    fun readBlocking(): Double {
        val latency = randomLatency(READ_MIN, READ_MAX)
        try {
            Thread.sleep(latency) // 스레드 블로킹
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        return 50.0
    }

    fun writeBlocking() {
        val latency = randomLatency(WRITE_MIN, WRITE_MAX)
        try {
            Thread.sleep(latency)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    // ==========================================
    // 2. Non-Blocking 메서드 (delay)
    // ==========================================
    suspend fun readNonBlocking(): Double {
        val latency = randomLatency(READ_MIN, READ_MAX)
        delay(latency) // 스레드 양보 (Suspend)
        return 50.0
    }

    suspend fun writeNonBlocking() {
        val latency = randomLatency(WRITE_MIN, WRITE_MAX)
        delay(latency) // 스레드 양보 (Suspend)
    }
}