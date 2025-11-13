package finn.service.k6

import kotlinx.coroutines.delay
import org.springframework.stereotype.Service

@Service
class K6SimulationPredictionService() {
    // DB 조회 평균 소요 시간 (예: 10ms)
    private val READ_LATENCY = 10L

    // DB 저장 평균 소요 시간 (예: 20ms - 인덱스 업데이트, 커밋 등 고려)
    private val WRITE_LATENCY = 20L

    // 1. 조회 (SELECT) 시뮬레이션
    fun readBlocking(): Double {
        Thread.sleep(READ_LATENCY) // 스레드 블로킹
        return 50.0 // 더미 데이터 반환
    }

    suspend fun readNonBlocking(): Double {
        delay(READ_LATENCY) // 스레드 양보 (Non-blocking)
        return 50.0
    }

    // 2. 저장 (UPDATE/INSERT) 시뮬레이션
    fun writeBlocking() {
        Thread.sleep(WRITE_LATENCY) // 스레드 블로킹
    }

    suspend fun writeNonBlocking() {
        delay(WRITE_LATENCY) // 스레드 양보 (Non-blocking)
    }
}