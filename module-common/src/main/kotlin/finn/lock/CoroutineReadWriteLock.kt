package finn.lock

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap
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

    // Key별 뮤텍스 관리 (Reference Count 포함)
    private val keyMutexMap = ConcurrentHashMap<UUID, RefCountedMutex>()
    private val mapLock = Mutex() // Map 조작(생성/삭제)의 원자성 보장

    // 내부 클래스: Mutex와 참조 카운트 관리
    private data class RefCountedMutex(
        val mutex: Mutex,
        val refCount: AtomicInteger
    )

    /**
     * 읽기 락을 획득하고 주어진 액션을 실행합니다.
     * 여러 읽기 작업이 병렬로 실행될 수 있습니다.
     */
    suspend fun <T> read(
        key: UUID,
        timeoutMillis: Long = 1 * 60 * 1000L,
        action: suspend () -> T
    ): T? {
        return withTimeout(timeoutMillis) {
            // 1. 전역 읽기 권한 획득
            readerGate.withLock {
                activeReaders.incrementAndGet()
            }

            try {
                // 2. Key별 Mutex 획득 (Reference Counting 적용)
                val keyedMutex = acquireKeyMutex(key)

                try {
                    // 3. 실제 Key별 락 획득 후 작업 수행
                    keyedMutex.withLock {
                        action()
                    }
                } finally {
                    // 4. Key Mutex 반납 및 정리
                    releaseKeyMutex(key)
                }
            } finally {
                // 5. 전역 읽기 카운트 감소
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
        return withTimeout(timeoutMillis) {
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

    private suspend fun acquireKeyMutex(key: UUID): Mutex {
        mapLock.withLock {
            // 해당 tickerId의 뮤텍스가 없으면 초기화
            val entry = keyMutexMap.getOrPut(key) {
                RefCountedMutex(Mutex(), AtomicInteger(0))
            }
            entry.refCount.incrementAndGet()
            return entry.mutex
        }
    }

    private suspend fun releaseKeyMutex(key: UUID) {
        mapLock.withLock {
            val entry = keyMutexMap[key] ?: return
            if (entry.refCount.decrementAndGet() <= 0) {
                keyMutexMap.remove(key) // 더 이상 대기자가 없으면 메모리에서 제거
            }
        }
    }
}