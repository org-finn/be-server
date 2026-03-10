import finn.entity.query.MarketStatus
import finn.manager.CandleData
import finn.manager.TickerRealTimeCandleManager
import finn.repository.GraphRepository
import finn.repository.MarketStatusRepository
import finn.scheduler.TickerRealTimePricePersistenceScheduler
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class TickerRealTimePricePersistenceSchedulerTest : BehaviorSpec({

    // 1. Mocks
    val candleManager = mockk<TickerRealTimeCandleManager>(relaxed = true)
    val graphRepository = mockk<GraphRepository>(relaxed = true)
    val marketStatusRepository = mockk<MarketStatusRepository>()

    // 2. Constants
    val KST_ZONE = ZoneId.of("Asia/Seoul")
    val UTC_ZONE = ZoneId.of("UTC")
    val TICKER_ID = UUID.randomUUID().toString()

    // 3. Helper to create fixed clock
    fun createFixedClock(dateTimeKst: LocalDateTime): Clock {
        return Clock.fixed(dateTimeKst.atZone(KST_ZONE).toInstant(), UTC_ZONE)
    }

    // 각 테스트 케이스마다 Mock 상태 초기화 (Isolation)
    isolationMode = io.kotest.core.spec.IsolationMode.InstancePerLeaf

    Given("겨울철 정규장 (Standard Time)") {
        // 상황: 미국 개장 09:30 EST -> 한국 23:30 KST
        // 현재 시각: 2026-02-02 23:40:00 (장 시작 10분 후)
        val currentDateTime = LocalDateTime.of(2026, 2, 2, 23, 40, 0)
        val fixedClock = createFixedClock(currentDateTime)

        // Scheduler 생성
        val scheduler = TickerRealTimePricePersistenceScheduler(
            candleManager, graphRepository, marketStatusRepository, fixedClock
        )

        // Mocking: 오늘 장 정보 (23:30~06:00)
        val todayKst = LocalDate.of(2026, 2, 2)
        val mockStatus = MarketStatus.create(todayKst, "23:30~06:00", null)

        every { marketStatusRepository.getOptionalMarketStatus(any()) } returns mockStatus

        // Mocking: 캔들 데이터 존재 (23:35분 캔들 -> 개장 5분 후)
        val candleTime = LocalDateTime.of(2026, 2, 2, 23, 35, 0)
        val mockCandle = CandleData(
            open = 100.0, high = 110.0, low = 90.0, close = 105.0,
            volume = 1000, startTime = candleTime
        )

        every { candleManager.isEmpty() } returns false
        every { candleManager.popAllCandles() } returns mapOf(TICKER_ID to mockCandle)

        When("스케줄러가 실행되면") {
            scheduler.flushCandlesToDb()

            Then("MaxLen은 390이고, 5분 뒤 캔들의 Index는 5여야 한다") {
                val indexSlot = slot<Int>()
                val maxLenSlot = slot<Int>()

                verify(exactly = 1) {
                    graphRepository.saveRealTimeTickerPrice(
                        tickerId = any(),
                        startTime = any(),
                        close = any(),
                        index = capture(indexSlot),
                        maxLen = capture(maxLenSlot)
                    )
                }

                maxLenSlot.captured shouldBe 390 // 6시간 30분
                indexSlot.captured shouldBe 5    // 23:30 시작 ~ 23:35 캔들
            }
        }
    }

    Given("여름철 서머타임 (DST)") {
        // 상황: 미국 개장 09:30 EDT -> 한국 22:30 KST
        // 현재 시각: 2026-06-15 22:40:00
        val currentDateTime = LocalDateTime.of(2026, 6, 15, 22, 40, 0)
        val fixedClock = createFixedClock(currentDateTime)

        val scheduler = TickerRealTimePricePersistenceScheduler(
            candleManager, graphRepository, marketStatusRepository, fixedClock
        )

        // Mocking: 서머타임 장 정보 (22:30~05:00)
        val todayKst = LocalDate.of(2026, 6, 15)
        val mockStatus = MarketStatus.create(todayKst, "22:30~05:00", null)

        every { marketStatusRepository.getOptionalMarketStatus(any()) } returns mockStatus

        // Mocking: 22:35분 캔들 (개장 5분 후)
        val candleTime = LocalDateTime.of(2026, 6, 15, 22, 35, 0)
        val mockCandle = CandleData(
            100.0, 110.0, 90.0, 105.0, 1000, candleTime
        )

        every { candleManager.isEmpty() } returns false
        every { candleManager.popAllCandles() } returns mapOf(TICKER_ID to mockCandle)

        When("스케줄러가 실행되면") {
            scheduler.flushCandlesToDb()

            Then("MaxLen은 390이고, 5분 뒤 캔들의 Index는 5여야 한다") {
                val indexSlot = slot<Int>()
                val maxLenSlot = slot<Int>()

                verify {
                    graphRepository.saveRealTimeTickerPrice(
                        any(), any(), any(), capture(indexSlot), capture(maxLenSlot)
                    )
                }

                maxLenSlot.captured shouldBe 390
                indexSlot.captured shouldBe 5 // 22:30 시작 ~ 22:35 캔들
            }
        }
    }

    Given("조기 마감일 (Early Close)") {
        // 상황: 미국 13:00 마감 -> 한국 03:00 마감 (겨울 기준)
        // 현재 시각: 2026-11-28 03:05:00 KST (장 마감 직후)
        // 날짜: 11월 28일 새벽이지만, 장 정보는 보통 전날(27일) 혹은 당일 새벽 기준 매핑됨
        val currentDateTime = LocalDateTime.of(2026, 11, 28, 3, 5, 0)
        val fixedClock = createFixedClock(currentDateTime)

        val scheduler = TickerRealTimePricePersistenceScheduler(
            candleManager, graphRepository, marketStatusRepository, fixedClock
        )

        // Mocking: 조기 마감 정보 (23:30~03:00) -> 총 3시간 30분(210분)
        // 로직상 28일자로 조회 시 해당 정보가 나온다고 가정
        val todayKst = LocalDate.of(2026, 11, 28)
        val mockStatus = MarketStatus.create(todayKst, "23:30~03:00", "Early Close")

        every { marketStatusRepository.getOptionalMarketStatus(any()) } returns mockStatus

        // Mocking: 02:59분 캔들 (마감 1분 전)
        val candleTime = LocalDateTime.of(2026, 11, 28, 2, 59, 0)
        val mockCandle = CandleData(
            100.0, 110.0, 90.0, 105.0, 1000, candleTime
        )

        every { candleManager.isEmpty() } returns false
        every { candleManager.popAllCandles() } returns mapOf(TICKER_ID to mockCandle)

        When("스케줄러가 실행되면") {
            scheduler.flushCandlesToDb()

            Then("MaxLen은 210분이고, 마감 1분 전 캔들의 Index는 209여야 한다") {
                val indexSlot = slot<Int>()
                val maxLenSlot = slot<Int>()

                verify {
                    graphRepository.saveRealTimeTickerPrice(
                        any(), any(), any(), capture(indexSlot), capture(maxLenSlot)
                    )
                }

                maxLenSlot.captured shouldBe 210 // 3시간 30분
                // 23:30 -> 02:59까지 경과 시간:
                // 23:30 ~ 02:30 (180분) + 29분 = 209분
                indexSlot.captured shouldBe 209
            }
        }
    }
})