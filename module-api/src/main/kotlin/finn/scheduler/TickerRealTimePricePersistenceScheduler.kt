package finn.scheduler

import finn.entity.query.MarketStatus
import finn.manager.TickerRealTimeCandleManager
import finn.repository.GraphRepository
import finn.repository.MarketStatusRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Component
class TickerRealTimePricePersistenceScheduler(
    private val candleManager: TickerRealTimeCandleManager,
    private val graphRepository: GraphRepository,
    private val marketStatusRepository: MarketStatusRepository,
    private val clock: Clock,
) {
    private val log = KotlinLogging.logger {}
    private val TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm")

    /**
     * 매분 0초에 실행 (Cron: 초 분 시 일 월 요일)
     */
    @Scheduled(cron = "0 * * * * *")
    @Async("dbExecutor") // 비동기 실행 (설정된 ThreadPool 사용)
    fun flushCandlesToDb() {
        // 0. 장이 닫혀있을때는 스케줄러 조기 종료
        val marketStatus =
            marketStatusRepository.getOptionalMarketStatus(LocalDate.now(clock))
        if (!MarketStatus.checkIsOpened(marketStatus, clock)) {
            return
        }

        // 1. 메모리에서 완성된 1분봉 데이터들을 모두 꺼냄 (Atomic Pop)
        val snapshot = candleManager.popAllCandles()

        if (snapshot.isEmpty()) {
            return
        }

        log.info { "Flushing ${snapshot.size} candles to DynamoDB..." }

        // 2. 엔티티 변환 및 저장
        snapshot.forEach { (tickerId, candleData) ->
            // CandleData의 시작 시간을 SK로 사용 (정확도 ↑)
            val timeKey = candleData.startTime.format(TIME_FORMATTER)

            graphRepository.saveRealTimeTickerPrice(
                UUID.fromString(tickerId), timeKey,
                candleData.open,
                candleData.high,
                candleData.low,
                candleData.close,
                candleData.volume
            )
        }

        log.info { "Flush completed." }
    }
}