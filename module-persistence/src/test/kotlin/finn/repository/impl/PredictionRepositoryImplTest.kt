package finn.repository.impl

import finn.TestApplication
import finn.repository.PredictionRepository
import finn.table.PredictionTable
import finn.table.TickerTable
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeSortedWith
import io.kotest.matchers.shouldBe
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime

@SpringBootTest(classes = [TestApplication::class])
internal class PredictionRepositoryImplTest(
    val predictionRepository: PredictionRepository
) : BehaviorSpec({

    beforeTest {
        // 테스트 데이터 준비: 최신 날짜를 동일하게 맞춤
        val latestDate = LocalDateTime.now()
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
                it[createdAt] = LocalDateTime.now()
            } get TickerTable.id // insert 후 생성된 ID를 바로 가져옴

            val ticker2Id = TickerTable.insert {
                it[code] = "BBBB"
                it[fullCompanyName] = "Company B Inc."
                it[shortCompanyName] = "Company B"
                it[country] = "USA"
                it[marketCap] = 2000L
                it[createdAt] = LocalDateTime.now()
            } get TickerTable.id

            PredictionTable.insert {
                it[predictionDate] = latestDate
                it[score] = 70
                it[tickerId] = ticker1Id.value
                it[positiveNewsCount] = 10L
                it[negativeNewsCount] = 3L
                it[neutralNewsCount] = 5L
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
                it[positiveNewsCount] = 15L
                it[negativeNewsCount] = 1L
                it[neutralNewsCount] = 2L
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
                result.content[0].tickerCode shouldBe "BBBB" // marketCap 2000
                result.content[1].tickerCode shouldBe "AAAA" // marketCap 1000
            }
        }

        When("sort 파라미터가 'upward'일 때") {
            val result = transaction {
                predictionRepository.getPredictionList(page = 0, size = 10, sort = "upward")
            }

            Then("점수(score)가 낮은 순(오름차순)으로 정렬되어야 한다") {
                result.content shouldBeSortedWith(compareBy { it.sentimentScore })
                result.content[0].sentimentScore shouldBe 70
                result.content[1].sentimentScore shouldBe 90
            }
        }

        When("sort 파라미터가 'downward'일 때") {
            val result = transaction {
                predictionRepository.getPredictionList(page = 0, size = 10, sort = "downward")
            }

            Then("점수(score)가 높은 순(내림차순)으로 정렬되어야 한다") {
                result.content shouldBeSortedWith(compareByDescending { it.sentimentScore })
                result.content[0].sentimentScore shouldBe 90
                result.content[1].sentimentScore shouldBe 70
            }
        }
    }
})