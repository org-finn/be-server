package finn.orchestrator.k6

import finn.handler.k6.K6SimulationHandlerFactory
import finn.task.PredictionTask
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock

@Service
class BlockingOrchestrator(
    private val handlerFactory: K6SimulationHandlerFactory
) {
    private val rwLock = ReentrantReadWriteLock()
    private val mutexes = ConcurrentHashMap<UUID, ReentrantLock>()
    private val wildcard = UUID.fromString("00000000-0000-0000-0000-000000000000")

    // SQS 리스너가 호출한다고 가정
    fun simulate(task: PredictionTask) {
        val tickerId = task.tickerId
        val handler = handlerFactory.findHandler(task.type)

        if (tickerId == wildcard) {
            // 1. 쓰기 락 획득 시도 (이미 누가 점유 중이면 여기서 스레드 BLOCK)
            rwLock.writeLock().lock()
            try {
                handler.handleBlocking(task)
            } finally {
                rwLock.writeLock().unlock()
            }
        } else {
            // 1. 읽기 락 획득
            rwLock.readLock().lock()
            try {
                val lock = mutexes.computeIfAbsent(tickerId) { ReentrantLock() }

                // 2. 개별 티커 락 획득 (경합 시 스레드 BLOCK)
                lock.lock()
                try {
                    handler.handleBlocking(task)
                } finally {
                    lock.unlock()
                }
            } finally {
                rwLock.readLock().unlock()
            }
        }
    }
}