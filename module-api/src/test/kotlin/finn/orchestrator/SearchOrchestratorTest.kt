package finn.orchestrator

import finn.exception.DomainPolicyViolationException
import finn.mapper.SearchDtoMapper
import finn.queryDto.TickerQueryDto
import finn.service.TickerQueryService
import finn.validator.checkKeywordValid
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*

internal class SearchOrchestratorTest : BehaviorSpec({
    // 1. 의존성 Mocking
    val tickerQueryService = mockk<TickerQueryService>()

    // 테스트 대상 클래스 인스턴스 생성
    val searchOrchestrator = SearchOrchestrator(tickerQueryService)

    Given("유효한 검색 키워드가 주어졌을 때") {
        val keyword = "ap"
        // 1. 반환될 리스트에 포함될 개별 DTO를 Mocking합니다.
        val mockDto = mockk<TickerQueryDto>()
        every { mockDto.tickerId() } returns UUID.randomUUID()
        every { mockDto.tickerCode() } returns "AAPL"
        every { mockDto.shortCompanyName() } returns "Apple"
        every { mockDto.fullCompanyName() } returns "Apple Inc."

        // 2. queryResult를 List<TickerSearchQueryDto> 타입으로 변경합니다.
        val queryResult = listOf(mockDto)

        val expectedResponse = SearchDtoMapper.toDto(queryResult)

        // 3. Mock 객체들의 동작을 List 타입에 맞게 정의합니다.
        every { tickerQueryService.getTickerSearchList(keyword) } returns queryResult // List를 반환

        When("getTickerSearchPreviewList를 호출하면") {
            val response = searchOrchestrator.getTickerSearchPreviewList(keyword)

            Then("모든 의존성이 순서대로 호출되고, 최종 DTO가 반환되어야 한다") {
                // 4. 호출 검증 부분도 List를 사용하는지 확인합니다.
                verify {
                    checkKeywordValid(keyword)
                    tickerQueryService.getTickerSearchList(keyword)
                    SearchDtoMapper.toDto(queryResult)
                }
                response shouldBe expectedResponse
            }
        }
    }

    Given("유효하지 않은 검색 키워드가 주어졌을 때") {
        withData(
            nameFn = { keyword -> "키워드가 ${if (keyword == null) "null" else "'$keyword'"}일 때" },
            null,      // 1. null인 경우
            "   ",     // 2. 공백인 경우
            "a"        // 3. 너무 짧은 경우
        ) { invalidKeyword ->
            When("getTickerSearchPreviewList를 호출하면") {
                Then("BadRequestDomainPolicyViolationException 예외가 발생해야 한다") {
                    shouldThrow<DomainPolicyViolationException> {
                        searchOrchestrator.getTickerSearchPreviewList(invalidKeyword)
                    }
                }
            }
        }
    }
})
