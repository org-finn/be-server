package finn.service

import finn.filter.TickerSearchFilter
import finn.queryDto.TickerSearchQueryDto
import finn.repository.TickerRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.util.*

internal class TickerQueryServiceTest : BehaviorSpec({

    data class TickerSearchQueryDtoImpl(
        val shortCompanyName: String,
        val shortCompanyNameKr: String
    ) : TickerSearchQueryDto {
        override fun shortCompanyName(): String {
            return this.shortCompanyName
        }

        override fun shortCompanyNameKr(): String {
            return this.shortCompanyNameKr
        }

        override fun tickerId(): UUID {
            TODO("Not yet implemented")
        }

        override fun tickerCode(): String {
            TODO("Not yet implemented")
        }

        override fun fullCompanyName(): String {
            TODO("Not yet implemented")
        }
    }

    // 1. 테스트 대상을 선언합니다.
    lateinit var tickerQueryService: TickerQueryService
    // 2. 모킹할 의존성을 선언합니다.
    lateinit var tickerRepository: TickerRepository

    val tickerSearchFilter = TickerSearchFilter()

    // 3. 테스트에 사용할 가짜 데이터 목록을 미리 정의합니다.
    val fakeTickerList = listOf(
        TickerSearchQueryDtoImpl(shortCompanyName = "AMD", shortCompanyNameKr = "암드"),
        TickerSearchQueryDtoImpl(shortCompanyName = "Amazon", shortCompanyNameKr = "아마존"),
        TickerSearchQueryDtoImpl(
            shortCompanyName = "Microsoft",
            shortCompanyNameKr = "마이크로소프트"
        ),
        TickerSearchQueryDtoImpl(
            shortCompanyName = "Tesla",
            shortCompanyNameKr = "테슬라"
        )
    )

    // Given: 테스트 환경 설정
    Given("TickerQueryService와 mock Repository가 준비되었을 때") {
        // 4. mock 객체와 서비스 인스턴스를 생성합니다.
        tickerRepository = mockk()
        tickerQueryService = TickerQueryService(tickerRepository, tickerSearchFilter)

        // 5. repository의 findAll()이 호출되면, 미리 정의한 가짜 데이터를 반환하도록 설정합니다.
        every { tickerRepository.findAll() } returns fakeTickerList

        Context("getTickerSearchList 메서드는") {

            When("'Am'로 검색하면") {
                val result = tickerQueryService.getTickerSearchList("Am")

                Then("'A'로 시작하는 Ticker 두 개(Amazon, AMD)를 반환해야 한다") {
                    result shouldHaveSize 2
                    result.any { it.shortCompanyName() == "AMD" } shouldBe true
                    result.any { it.shortCompanyName() == "Amazon" } shouldBe true
                }
            }

            When("'Micro'로 검색하면") {
                val result = tickerQueryService.getTickerSearchList("Micro")

                Then("'Micro'로 시작하는 Ticker 한 개(Microsoft)를 반환해야 한다") {
                    result shouldHaveSize 1
                    result.first().shortCompanyName() shouldBe "Microsoft"
                }
            }

            When("'Meta'로 검색하면") {
                val result = tickerQueryService.getTickerSearchList("Meta")

                Then("결과가 없는 빈 리스트를 반환해야 한다") {
                    result.shouldBeEmpty()
                }
            }

            When("'테슬'로 검색하면") {
                val result = tickerQueryService.getTickerSearchList("테슬")

                Then("'테슬'로 시작하는 Ticker 한 개(테슬라)를 반환해야 한다") {
                    result shouldHaveSize 1
                    result.first().shortCompanyNameKr() shouldBe "테슬라"
                }
            }
        }
    }
})