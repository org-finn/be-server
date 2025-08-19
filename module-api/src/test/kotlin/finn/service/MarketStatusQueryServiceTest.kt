package finn.service

import finn.entity.MarketStatus
import finn.repository.MarketStatusRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

internal class MarketStatusQueryServiceTest : BehaviorSpec({
    val marketStatusRepository = mockk<MarketStatusRepository>()

    mockkObject(MarketStatus)

    afterEach {
        clearMocks(marketStatusRepository)
    }

    Given("오늘이 주말(토요일)일 때") {
        val saturday = LocalDate.of(2025, 8, 16)
        val fixedClock = Clock.fixed(
            saturday.atStartOfDay(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault()
        )

        // 4. 고정된 Clock을 서비스에 주입하여 테스트 대상 인스턴스 생성
        val service = MarketStatusQueryService(marketStatusRepository, fixedClock)

        val weekendStatus = MarketStatus.create(saturday, "휴장", "Weekend")

        mockkObject(MarketStatus) // MarketStatus.create 등 companion object는 여전히 mock
        every { MarketStatus.isWeekend(saturday) } returns true
        every { MarketStatus.getWeekendMarketStatus(saturday) } returns weekendStatus

        When("getTodayMarketStatus를 호출하면") {
            val result = service.getTodayMarketStatus()
            Then("DB 조회 없이 주말 상태를 반환해야 한다") {
                result shouldBe weekendStatus
            }
        }
    }

    Given("오늘이 평일이지만 DB에 휴장일(Independence Day)로 지정되어 있을 때") {
        val holiday = LocalDate.of(2025, 8, 15)
        val fixedClock = Clock.fixed(
            holiday.atStartOfDay(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault()
        )
        val service = MarketStatusQueryService(marketStatusRepository, fixedClock)

        val holidayStatus = MarketStatus.create(holiday, "휴장", "Independence Day")

        every { MarketStatus.isWeekend(holiday) } returns false
        every { marketStatusRepository.getOptionalMarketStatus(holiday) } returns holidayStatus

        When("getTodayMarketStatus를 호출하면") {
            val result = service.getTodayMarketStatus()
            Then("DB에서 조회한 휴장일 상태를 반환해야 한다") {
                result shouldBe holidayStatus
            }
        }
    }

    Given("오늘이 평범한 개장일일 때") {
        val businessDay = LocalDate.of(2025, 8, 18)
        val fixedClock = Clock.fixed(
            businessDay.atStartOfDay(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault()
        )
        val service = MarketStatusQueryService(marketStatusRepository, fixedClock)
        val openStatus = MarketStatus.create(businessDay, "09:00~16:30", null)

        every { MarketStatus.isWeekend(businessDay) } returns false
        every { marketStatusRepository.getOptionalMarketStatus(businessDay) } returns null
        every {
            MarketStatus.getFullOpenedMarketStatus(
                businessDay
            )
        } returns openStatus

        When("getTodayMarketStatus를 호출하면") {
            val result = service.getTodayMarketStatus()
            Then("완전 개장일 상태를 반환해야 한다") {
                result shouldBe openStatus
            }
        }
    }
})