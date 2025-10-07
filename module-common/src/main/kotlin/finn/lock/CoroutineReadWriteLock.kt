package finn.lock

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withTimeoutOrNull
import org.springframework.stereotype.Component

@Component
class CoroutineReadWriteLock(
    private val maxReaders: Int = 1000
) {

    companion object {
        private val log = KotlinLogging.logger {}
    }

    // 쓰기 작업의 독점적 실행을 보장하는 Mutex
    private val writeLock = Mutex()

    // 다수 읽기 작업을 허용하는 Semaphore
    private val readLock = Semaphore(permits = maxReaders)

    /**
     * 읽기 락을 획득하고 주어진 액션을 실행합니다.
     * 여러 읽기 작업이 동시에 이 블록을 실행할 수 있습니다.
     */
    suspend fun <T> read(
        timeoutMillis: Long = 5 * 60 * 1000L,
        action: suspend () -> T
    ): T? {
        return withTimeoutOrNull(timeoutMillis) {
            // 쓰기 락을 획득할 수 있어야 읽기 락 획득 가능
            writeLock.lock()
            log.debug { "읽기 작업에서 쓰기 락 획득 성공" }
            try {
                readLock.acquire()
                log.debug { "읽기 작업에서 읽기 락 획득 성공" }
            } catch (e: Exception) {
                writeLock.unlock()
                throw e
            }
            // 읽기 락을 획득하였으므로 쓰기 락 즉시 해제
            writeLock.unlock()
            log.debug { "읽기 작업에서 쓰기 락을 반납합니다." }

            try {
                action()
            } finally {
                readLock.release()
                log.debug { "읽기 작업에서 읽기 락을 반납합니다." }
            }
        }
    }

    suspend fun <T> write(
        timeoutMillis: Long = 10 * 60 * 1000L,
        action: suspend () -> T
    ): T? {
        return withTimeoutOrNull(timeoutMillis) {
            try {
                writeLock.lock()
                log.debug { "쓰기 작업에서 쓰기 락 획득 성공" }
                var acquiredPermits = 0 // 성공적으로 획득한 퍼밋 수를 추적할 변수

                try {
                    // 현재 진행 중인 모든 읽기 작업이 끝날 때까지 대기
                    // 모든 reader의 permit을 획득
                    repeat(maxReaders) {
                        readLock.acquire()
                        acquiredPermits++
                    }
                    log.debug { "쓰기 작업에서 모든 읽기 획득 성공" }
                    action()
                } finally {
                    // 모든 reader의 permit을 다시 반납
                    repeat(acquiredPermits) { readLock.release() }
                    log.debug { "쓰기 작업에서 모든 읽기 릴리즈 성공" }
                }
            } finally {
                writeLock.unlock()
                log.debug { "쓰기 작업에서 쓰기 락을 반납합니다." }
            }
        }
    }
}