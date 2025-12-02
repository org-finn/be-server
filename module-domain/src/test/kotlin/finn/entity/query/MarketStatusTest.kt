package finn.entity.query

import finn.converter.BusinessDayLocalizer
import finn.converter.BusinessDayLocalizer.Companion.getTradingHours
import finn.exception.DomainPolicyViolationException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import java.time.*

class MarketStatusTest : StringSpec({

    /**
     * 테스트 실행 전(beforeSpec)에 BusinessDayLocalizer Companion Object를 모킹하고,
     * 테스트 종료 후(afterSpec)에 모킹을 해제하여 다른 테스트에 영향을 주지 않도록 합니다.
     */
    beforeSpec {
        // BusinessDayLocalizer가 클래스이고 Companion Object를 가진 경우,
        // Kotlin에서는 클래스 이름으로 Companion Object 인스턴스를 참조할 수 있습니다.
        mockkObject(BusinessDayLocalizer)
    }

    afterSpec {
        unmockkObject(BusinessDayLocalizer)
    }

    fun createClockFromKst(kstDateTime: LocalDateTime): Clock {
        val kstZone = ZoneId.of("Asia/Seoul")
        // 입력받은 시간을 KST로 해석하여 Instant(절대 시각)로 변환
        val instant = kstDateTime.atZone(kstZone).toInstant()
        // 시스템은 UTC Clock을 사용한다고 가정
        return Clock.fixed(instant, ZoneId.of("UTC"))
    }

    // 공통 테스트 데이터
    val tradingDate = LocalDate.of(2025, 11, 28)
    val normalMarket = MarketStatus.create(tradingDate, "09:00~14:00", null)
    val overnightMarket = MarketStatus.create(tradingDate, "22:00~03:00", null)
    val closedMarket = MarketStatus.create(tradingDate, "휴장", "Holiday")

    val invalidFormatMarket = MarketStatus.create(tradingDate, "09:00-14:00", null)
    val invalidTimeMarket = MarketStatus.create(tradingDate, "AA:BB~14:00", null)

    // --- Static Method Mocking이 필요한 테스트 ---

    "marketStatus가 null이면 기본 개장 시간(09:00~14:00)을 적용하여 판단한다" {
        // [Given] KST 10:00 (개장 중)
        val clock = createClockFromKst(LocalDateTime.of(tradingDate, LocalTime.of(10, 0)))

        // [Mocking]
        every { getTradingHours() } returns "09:00~14:00"

        // [When & Then]
        MarketStatus.checkIsOpened(null, clock).shouldBeTrue()
    }

    "marketStatus가 null이고 현재 시각이 기본 장 운영 시간 외라면 false를 반환한다" {
        // [Given] KST 15:00 (장 마감 후)
        val clock = createClockFromKst(LocalDateTime.of(tradingDate, LocalTime.of(15, 0)))

        // [Mocking]
        every { getTradingHours() } returns "09:00~14:00"

        // [When & Then]
        MarketStatus.checkIsOpened(null, clock).shouldBeFalse()
    }


    // --- 기존 로직 테스트 ---

    "휴장일이면 false를 반환해야 한다" {
        val clock = createClockFromKst(LocalDateTime.of(tradingDate, LocalTime.of(10, 0)))
        MarketStatus.checkIsOpened(closedMarket, clock).shouldBeFalse()
    }

    // --- 일반적인 장 시간 테스트 (09:00 ~ 14:00) ---

    "정규장 내 시각 (10:30) 에는 true를 반환해야 한다" {
        val fixedTime = LocalDateTime.of(tradingDate, LocalTime.of(10, 30))
        val clock = createClockFromKst(fixedTime)
        MarketStatus.checkIsOpened(normalMarket, clock).shouldBeTrue()
    }

    "개장 시각 경계 (09:00) 에는 true를 반환해야 한다" {
        val fixedTime = LocalDateTime.of(tradingDate, LocalTime.of(9, 0))
        val clock = createClockFromKst(fixedTime)
        MarketStatus.checkIsOpened(normalMarket, clock).shouldBeTrue()
    }

    "폐장 시각 직전 (13:59) 에는 true를 반환해야 한다" {
        val fixedTime = LocalDateTime.of(tradingDate, LocalTime.of(13, 59))
        val clock = createClockFromKst(fixedTime)
        MarketStatus.checkIsOpened(normalMarket, clock).shouldBeTrue()
    }

    "폐장 시각 (14:00) 에는 false를 반환해야 한다" {
        val fixedTime = LocalDateTime.of(tradingDate, LocalTime.of(14, 0))
        val clock = createClockFromKst(fixedTime)
        MarketStatus.checkIsOpened(normalMarket, clock).shouldBeFalse()
    }

    "개장 시각 이전 (08:59) 에는 false를 반환해야 한다" {
        val fixedTime = LocalDateTime.of(tradingDate, LocalTime.of(8, 59))
        val clock = createClockFromKst(fixedTime)
        MarketStatus.checkIsOpened(normalMarket, clock).shouldBeFalse()
    }

    "폐장 시각 이후 (14:01) 에는 false를 반환해야 한다" {
        val fixedTime = LocalDateTime.of(tradingDate, LocalTime.of(14, 1))
        val clock = createClockFromKst(fixedTime)
        MarketStatus.checkIsOpened(normalMarket, clock).shouldBeFalse()
    }

    // --- 자정을 넘어가는 장 시간 테스트 (22:00 ~ 03:00) ---

    "자정 초과 장 시작 시각 (22:00) 에는 true를 반환해야 한다" {
        val fixedTime = LocalDateTime.of(tradingDate, LocalTime.of(22, 0))
        val clock = createClockFromKst(fixedTime)
        MarketStatus.checkIsOpened(overnightMarket, clock).shouldBeTrue()
    }

    "자정 초과 장 종료 시각 직전 (02:59) 에는 true를 반환해야 한다" {
        // KST 기준 다음 날 새벽 02:59
        val fixedTime = LocalDateTime.of(tradingDate.plusDays(1), LocalTime.of(2, 59))
        val clock = createClockFromKst(fixedTime)
        MarketStatus.checkIsOpened(overnightMarket, clock).shouldBeTrue()
    }

    "자정을 넘은 장 시간 내 시각 (00:30) 에는 true를 반환해야 한다" {
        val fixedTime = LocalDateTime.of(tradingDate.plusDays(1), LocalTime.of(0, 30))
        val clock = createClockFromKst(fixedTime)
        MarketStatus.checkIsOpened(overnightMarket, clock).shouldBeTrue()
    }

    "자정 초과 장 시작 시각 이전 (21:59) 에는 false를 반환해야 한다" {
        val fixedTime = LocalDateTime.of(tradingDate, LocalTime.of(21, 59))
        val clock = createClockFromKst(fixedTime)
        MarketStatus.checkIsOpened(overnightMarket, clock).shouldBeFalse()
    }

    "자정 초과 장 종료 시각 (03:00) 에는 false를 반환해야 한다" {
        val fixedTime = LocalDateTime.of(tradingDate.plusDays(1), LocalTime.of(3, 0))
        val clock = createClockFromKst(fixedTime)
        MarketStatus.checkIsOpened(overnightMarket, clock).shouldBeFalse()
    }

    // --- 예외 처리 테스트 ---
    "TradingHours 형식이 올바르지 않으면 DomainPolicyViolationException을 던져야 한다" {
        val clock = createClockFromKst(LocalDateTime.of(tradingDate, LocalTime.of(10, 0)))
        shouldThrow<DomainPolicyViolationException> {
            MarketStatus.checkIsOpened(invalidFormatMarket, clock)
        }
        shouldThrow<DomainPolicyViolationException> {
            MarketStatus.checkIsOpened(invalidTimeMarket, clock)
        }
    }
})