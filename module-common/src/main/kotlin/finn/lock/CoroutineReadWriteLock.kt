package finn.lock

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger

@Component
class CoroutineReadWriteLock {

    companion object {
        private val log = KotlinLogging.logger {}
    }

    // 활성 읽기 작업의 수를 카운트
    private val activeReaders = AtomicInteger(0)

    // 쓰기 작업 접근을 제어 (오직 하나의 쓰기 작업만 허용)
    private val writerMutex = Mutex()

    // 읽기 작업이 통과해야 하는 게이트, 쓰기 작업이 이 게이트를 잠가 새로운 읽기 작업을 차단
    private val readerGate = Mutex()

    /**
     * 읽기 락을 획득하고 주어진 액션을 실행합니다.
     * 여러 읽기 작업이 병렬로 실행될 수 있습니다.
     */
    suspend fun <T> read(
        timeoutMillis: Long = 1 * 60 * 1000L,
        action: suspend () -> T
    ): T? {
        return withTimeoutOrNull(timeoutMillis) {
            // 현재 진행중인 쓰기 작업이 없다면 해당 게이트를 통과함
            readerGate.withLock {
                // 활성 읽기 작업 카운트를 증가
                activeReaders.incrementAndGet()
            }

            try {
                action()
            } finally {
                // 완료되면 카운트를 감소(반드시 실행됨)
                activeReaders.decrementAndGet()
            }
        }
    }

    /**
     * 배타적인 쓰기 락을 획득하고 주어진 액션을 실행합니다.
     * 완료될 때까지 모든 새로운 읽기/쓰기 작업을 차단합니다.
     */
    suspend fun <T> write(
        timeoutMillis: Long = 3 * 60 * 1000L,
        action: suspend () -> T
    ): T? {
        return withTimeoutOrNull(timeoutMillis) {
            // 오직 하나의 쓰기 작업만 이 블록에 진입하도록 보장
            writerMutex.withLock {

                // readerGate를 잠구어, 새로운 읽기 작업 진입 차단
                readerGate.lock()

                try {
                    // (이미 실행 중이던) 모든 활성 읽기 작업이 끝날 때까지 대기
                    while (activeReaders.get() > 0) {
                        log.debug { "Writer waiting for ${activeReaders.get()} active readers to finish..." }
                        delay(10) // spin-wait (대기)
                    }

                    // 이 시점: activeReaders == 0 이고, 새로운 읽기 작업은 차단된 상태
                    // 쓰기 작업을 시작할 수 있는 상태
                    log.debug { "Writer acquired exclusive lock. Executing action..." }
                    action()

                } finally {
                    // readerGate의 락을 해제하여, 새로운 읽기 작업이 진행되도록 허용
                    readerGate.unlock()
                }
            }
        }
    }
}