package finn.repository.impl

import finn.TestApplication
import finn.exception.CriticalDataPollutedException
import finn.repository.PredictionRepository
import finn.table.PredictionTable
import finn.table.TickerTable
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.time.ZoneId

@SpringBootTest(classes = [TestApplication::class])
internal class PredictionRepositoryImplTest(
    val predictionRepository: PredictionRepository
) : BehaviorSpec({

    beforeTest {
        // 테스트 데이터 준비: 최신 날짜를 동일하게 맞춤
        val latestDate = LocalDateTime.now(ZoneId.of("UTC"))
        transaction {
            PredictionTable.deleteAll()
            TickerTable.deleteAll()

            // 1. TickerTable에 직접 insert 쿼리 실행
            val ticker1Id = TickerTable.insert {
                it[code] = "AAAA"
                it[fullCompanyName] = "Company A Inc."
                it[shortCompanyName] = "Company A"
                it[country] = "USA"
                it[marketCap] = 1000L
                it[exchangeCode] = "CODE"
                it[createdAt] = LocalDateTime.now()
            } get TickerTable.id // insert 후 생성된 ID를 바로 가져옴

            val ticker2Id = TickerTable.insert {
                it[code] = "BBBB"
                it[fullCompanyName] = "Company B Inc."
                it[shortCompanyName] = "Company B"
                it[country] = "USA"
                it[marketCap] = 2000L
                it[exchangeCode] = "CODE"
                it[createdAt] = LocalDateTime.now()
            } get TickerTable.id

            PredictionTable.insert {
                it[predictionDate] = latestDate
                it[score] = 70
                it[tickerId] = ticker1Id.value
                it[positiveArticleCount] = 10L
                it[negativeArticleCount] = 3L
                it[neutralArticleCount] = 5L
                it[sentiment] = 1 // 긍정을 나타내는 예시값
                it[strategy] = "WEEK_BUY" // 70점에 해당하는 예시 전략
                it[tickerCode] = "AAAA"
                it[shortCompanyName] = "CompA"
                it[createdAt] = LocalDateTime.now()
            }
            PredictionTable.insert {
                it[predictionDate] = latestDate
                it[score] = 90
                it[tickerId] = ticker2Id.value
                it[positiveArticleCount] = 15L
                it[negativeArticleCount] = 1L
                it[neutralArticleCount] = 2L
                it[sentiment] = 1 // 긍정을 나타내는 예시값
                it[strategy] = "STRONG_BUY" // 90점에 해당하는 예시 전략
                it[tickerCode] = "BBBB"
                it[shortCompanyName] = "CompB"
                it[createdAt] = LocalDateTime.now()
            }
        }
    }

    Given("getPredictionList 메서드에") {
        When("sort 파라미터가 'popular'일 때") {
            val result = transaction {
                predictionRepository.getPredictionList(page = 0, size = 10, sort = "popular")
            }

            Then("시가총액(marketCap)이 높은 순으로 정렬되어야 한다") {
                result.content.size shouldBe 2
                result.content[0].tickerCode() shouldBe "BBBB" // marketCap 2000
                result.content[1].tickerCode() shouldBe "AAAA" // marketCap 1000
            }
        }

        When("sort 파라미터가 'upward'일 때") {
            val result = transaction {
                predictionRepository.getPredictionList(page = 0, size = 10, sort = "upward")
            }

            Then("점수(score)가 낮은 순(오름차순)으로 정렬되어야 한다") {
                result.content[0].tickerCode() shouldBe "AAAA"
                result.content[1].tickerCode() shouldBe "BBBB"
            }
        }

        When("sort 파라미터가 'downward'일 때") {
            val result = transaction {
                predictionRepository.getPredictionList(page = 0, size = 10, sort = "downward")
            }

            Then("점수(score)가 높은 순(내림차순)으로 정렬되어야 한다") {
                result.content[0].tickerCode() shouldBe "BBBB"
                result.content[1].tickerCode() shouldBe "AAAA"
            }
        }

        When("sort가 지원하지 않는 옵션일 때") {
            val invalidSort = "unsupported_option"

            Then("ServerErrorCriticalDataPollutedException 예외가 발생해야 한다") {
                // shouldThrow 블록 안에서 예외가 발생하면 테스트 성공
                shouldThrow<CriticalDataPollutedException> {
                    transaction {
                        predictionRepository.getPredictionList(
                            page = 0,
                            size = 10,
                            sort = invalidSort
                        )
                    }
                }
            }
        }
    }

//    Given("오늘 및 어제 날짜의 예측 점수가 여러 티커에 대해 저장되어 있을 때") {
//        When("특정 티커 A의 오늘 점수 조회를 요청하면") {
//            val result = predictionRepository.getRecentSentimentScoreList(tickerA.id.value)
//
//            Then("해당 티커의 오늘 날짜 점수만 리스트로 반환해야 한다") {
//                // Ticker A의 오늘 점수는 2개
//                result shouldHaveSize 2
//                // 반환된 점수가 80, 85인지 순서에 상관없이 확인
//                result shouldContainExactlyInAnyOrder listOf(80, 85)
//            }
//        }
//    }
})