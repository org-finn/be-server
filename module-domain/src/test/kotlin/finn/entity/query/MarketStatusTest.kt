package finn.entity.query

import finn.exception.DomainPolicyViolationException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import java.time.*

class MarketStatusTest : StringSpec({
    /**
     * 테스트를 위한 고정된 시각을 제공하는 Clock 구현체
     * UTC TimeZone을 사용합니다.
     */
    fun createFixedClock(dateTime: LocalDateTime): Clock {
        val instant = dateTime.toInstant(ZoneOffset.UTC)
        return Clock.fixed(instant, ZoneId.of("UTC"))
    }

    // 공통적으로 사용할 MarketStatus 객체 (09:00 ~ 14:00)
    val tradingDate = LocalDate.of(2025, 11, 28)
    val normalMarket = MarketStatus.create(tradingDate, "09:00~14:00", null)
    val overnightMarket = MarketStatus.create(tradingDate, "22:00~03:00", null)
    val closedMarket =
        MarketStatus.create(tradingDate, MarketStatus.getClosedDayTradingHours(), "Holiday")

    val invalidFormatMarket = MarketStatus.create(tradingDate, "09:00-14:00", null)
    val invalidTimeMarket = MarketStatus.create(tradingDate, "AA:BB~14:00", null)

    "marketStatus가 null이면 false를 반환해야 한다" {
        val clock = createFixedClock(LocalDateTime.of(tradingDate, LocalTime.of(10, 0)))
        MarketStatus.checkIsOpened(null, clock).shouldBeFalse()
    }

    "휴장일이면 false를 반환해야 한다" {
        val clock = createFixedClock(LocalDateTime.of(tradingDate, LocalTime.of(10, 0)))
        MarketStatus.checkIsOpened(closedMarket, clock).shouldBeFalse()
    }

    // --- 일반적인 장 시간 테스트 (09:00 ~ 14:00) ---

    "정규장 내 시각 (10:30) 에는 true를 반환해야 한다" {
        val fixedTime = LocalDateTime.of(tradingDate, LocalTime.of(10, 30))
        val clock = createFixedClock(fixedTime)
        MarketStatus.checkIsOpened(normalMarket, clock).shouldBeTrue()
    }

    "개장 시각 경계 (09:00) 에는 true를 반환해야 한다" {
        val fixedTime = LocalDateTime.of(tradingDate, LocalTime.of(9, 0))
        val clock = createFixedClock(fixedTime)
        MarketStatus.checkIsOpened(normalMarket, clock).shouldBeTrue()
    }

    "폐장 시각 직전 (13:59) 에는 true를 반환해야 한다" {
        val fixedTime = LocalDateTime.of(tradingDate, LocalTime.of(13, 59))
        val clock = createFixedClock(fixedTime)
        MarketStatus.checkIsOpened(normalMarket, clock).shouldBeTrue()
    }

    "폐장 시각 (14:00) 에는 false를 반환해야 한다" {
        val fixedTime = LocalDateTime.of(tradingDate, LocalTime.of(14, 0))
        val clock = createFixedClock(fixedTime)
        MarketStatus.checkIsOpened(normalMarket, clock).shouldBeFalse()
    }

    "개장 시각 이전 (08:59) 에는 false를 반환해야 한다" {
        val fixedTime = LocalDateTime.of(tradingDate, LocalTime.of(8, 59))
        val clock = createFixedClock(fixedTime)
        MarketStatus.checkIsOpened(normalMarket, clock).shouldBeFalse()
    }

    "폐장 시각 이후 (14:01) 에는 false를 반환해야 한다" {
        val fixedTime = LocalDateTime.of(tradingDate, LocalTime.of(14, 1))
        val clock = createFixedClock(fixedTime)
        MarketStatus.checkIsOpened(normalMarket, clock).shouldBeFalse()
    }

    // --- 자정을 넘어가는 장 시간 테스트 (22:00 ~ 03:00) ---

    "자정 초과 장 시작 시각 (22:00) 에는 true를 반환해야 한다" {
        val fixedTime = LocalDateTime.of(tradingDate, LocalTime.of(22, 0))
        val clock = createFixedClock(fixedTime)
        MarketStatus.checkIsOpened(overnightMarket, clock).shouldBeTrue()
    }

    "자정 초과 장 종료 시각 직전 (02:59) 에는 true를 반환해야 한다" {
        // MarketStatus의 date는 당일이지만, 02:59는 사실상 다음 날 새벽입니다.
        // checkIsOpened는 LocalTime만 비교하므로, tradingDate를 사용해도 무방합니다.
        val fixedTime = LocalDateTime.of(tradingDate.plusDays(1), LocalTime.of(2, 59))
        val clock = createFixedClock(fixedTime)
        MarketStatus.checkIsOpened(overnightMarket, clock).shouldBeTrue()
    }

    "자정을 넘은 장 시간 내 시각 (00:30) 에는 true를 반환해야 한다" {
        val fixedTime = LocalDateTime.of(tradingDate.plusDays(1), LocalTime.of(0, 30))
        val clock = createFixedClock(fixedTime)
        MarketStatus.checkIsOpened(overnightMarket, clock).shouldBeTrue()
    }

    "자정 초과 장 시작 시각 이전 (21:59) 에는 false를 반환해야 한다" {
        val fixedTime = LocalDateTime.of(tradingDate, LocalTime.of(21, 59))
        val clock = createFixedClock(fixedTime)
        MarketStatus.checkIsOpened(overnightMarket, clock).shouldBeFalse()
    }

    "자정 초과 장 종료 시각 (03:00) 에는 false를 반환해야 한다" {
        val fixedTime = LocalDateTime.of(tradingDate.plusDays(1), LocalTime.of(3, 0))
        val clock = createFixedClock(fixedTime)
        MarketStatus.checkIsOpened(overnightMarket, clock).shouldBeFalse()
    }

    // --- 예외 처리 테스트 ---

    "TradingHours 형식이 올바르지 않으면 DomainPolicyViolationException을 던져야 한다 (구분자 오류)" {
        val fixedTime = LocalDateTime.of(tradingDate, LocalTime.of(10, 0))
        val clock = createFixedClock(fixedTime)

        shouldThrow<DomainPolicyViolationException> {
            MarketStatus.checkIsOpened(invalidFormatMarket, clock)
        }
    }

    "TradingHours 시각 파싱이 실패하면 DomainPolicyViolationException을 던져야 한다 (시간 문자열 오류)" {
        val fixedTime = LocalDateTime.of(tradingDate, LocalTime.of(10, 0))
        val clock = createFixedClock(fixedTime)

        shouldThrow<DomainPolicyViolationException> {
            MarketStatus.checkIsOpened(invalidTimeMarket, clock)
        }
    }
})