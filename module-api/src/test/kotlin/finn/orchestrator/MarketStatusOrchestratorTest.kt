package finn.orchestrator

import finn.entity.query.MarketStatus
import finn.service.MarketStatusQueryService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate

internal class MarketStatusOrchestratorTest : BehaviorSpec({
    val marketStatusQueryService = mockk<MarketStatusQueryService>()
    val orchestrator = MarketStatusOrchestrator(marketStatusQueryService)

    Given("오늘의 시장 정보를 조회할 때") {
        withData(
            nameFn = { (status, _) -> "시장 상태가 '${status.tradingHours}'일 때" },
            // 1. 실제 데이터 형식을 반영한 테스트 케이스
            MarketStatus.create(LocalDate.now(), "휴장", "New Year's Day") to true,
            MarketStatus.create(LocalDate.now(), "22:30~05:00", null) to false,
            MarketStatus.create(LocalDate.now(), "22:30~02:00", "Independence Day") to false
        ) { (marketStatus, expectedIsClosed) ->

            every { marketStatusQueryService.getTodayMarketStatus() } returns marketStatus

            When("getTodayMarketStatus를 호출하면") {
                val response = orchestrator.getTodayMarketStatus()

                Then("isClosed 필드가 올바르게 계산되고, 나머지는 그대로 매핑되어야 한다") {
                    response.isHoliday shouldBe expectedIsClosed
                    response.tradingHours shouldBe marketStatus.tradingHours
                    response.eventName shouldBe marketStatus.eventName
                }
            }
        }
    }
})
