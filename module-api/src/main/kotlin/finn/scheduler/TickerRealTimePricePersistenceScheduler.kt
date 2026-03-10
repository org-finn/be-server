package finn.scheduler

import finn.entity.query.MarketStatus
import finn.manager.TickerRealTimeCandleManager
import finn.repository.GraphRepository
import finn.repository.MarketStatusRepository
import finn.transaction.ExposedTransactional
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.*
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
    private val UTC_ZONE = ZoneId.of("UTC")
    private val KST_ZONE = ZoneId.of("Asia/Seoul")

    @Scheduled(cron = "0 * * * * *")
    @Async("dbExecutor")
    @ExposedTransactional
    fun flushCandlesToDb() {
        // 1. 현재 시각 (UTC)
        val nowUTC = ZonedDateTime.now(clock.withZone(UTC_ZONE))
        val todayUTC = nowUTC.toLocalDate()
        val todayKst = nowUTC.withZoneSameInstant(KST_ZONE).toLocalDate()

        // 2. MarketStatus 조회 & 오픈 여부 체크
        val marketStatus = marketStatusRepository.getOptionalMarketStatus(todayUTC)

        val snapshot = candleManager.popAllCandles()
        if (snapshot.isEmpty()) return

        // 3. TradingHours 결정 & MaxLen 계산
        val currentTradingHours = MarketStatus.resolveTradingHours(marketStatus)
        val maxLen = MarketStatus.calculateMaxLen(currentTradingHours)

        // 4. 기준 시작 시간을 KST 문자열에서 파싱 -> UTC로 변환
        // 예: "23:30" 파싱
        val startTimeStr = currentTradingHours.split("~")[0]
        val marketOpenLocalTime =
            LocalTime.parse(startTimeStr, DateTimeFormatter.ofPattern("HH:mm"))

        // 4-1. KST 기준의 개장 시각 생성 (예: 2026-02-02 23:30:00+09:00)
        // 주의: todayKst를 사용해야 함. (미국장은 한국시간 밤에 열리므로 날짜가 같음)
        val marketOpenKST = ZonedDateTime.of(todayKst, marketOpenLocalTime, KST_ZONE)

        // 4-2. 이를 UTC로 변환 (예: 2026-02-02 14:30:00Z) -> Index 0의 기준점
        val marketOpenUTC = marketOpenKST.withZoneSameInstant(UTC_ZONE)

        log.info { "Persisting candles.. Time(KST): $marketOpenKST, Time(UTC): $marketOpenUTC, MaxLen: $maxLen" }

        snapshot.forEach { (tickerId, candle) ->
            // 5. 캔들 시간 (UTC)
            val candleTimeUTC = candle.startTime.atZone(KST_ZONE).withZoneSameInstant(UTC_ZONE)

            // 6. 인덱스 계산 (UTC 끼리 비교)
            var index = Duration.between(marketOpenUTC, candleTimeUTC).toMinutes().toInt()

            // 날짜 경계 보정 (혹시 모를 음수 방지)
            if (index < -720) index += 1440

            // 7. 인덱스 유효성 검사
            val safeIndex = when {
                index < 0 -> 0
                index >= maxLen -> maxLen - 1
                else -> index
            }

            // DB 저장
            graphRepository.saveRealTimeTickerPrice(
                tickerId = UUID.fromString(tickerId),
                startTime = candle.startTime,
                close = candle.close,
                index = safeIndex,
                maxLen = maxLen
            )
        }
    }
}