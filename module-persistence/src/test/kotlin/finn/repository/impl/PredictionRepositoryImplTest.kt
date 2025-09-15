import finn.TestApplication
import finn.entity.PredictionExposed
import finn.entity.command.PredictionC
import finn.exception.CriticalDataPollutedException
import finn.repository.PredictionRepository
import finn.table.PredictionTable
import finn.table.TickerTable
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@SpringBootTest(classes = [TestApplication::class])
internal class PredictionRepositoryImplTest(
    private val predictionRepository: PredictionRepository
) : BehaviorSpec({

    val ticker1Id = UUID.randomUUID()
    val ticker2Id = UUID.randomUUID()
    val latestDate = LocalDateTime.now(ZoneId.of("America/New_York"))

    beforeTest {
        transaction {
            PredictionTable.deleteAll()
            TickerTable.deleteAll()

            TickerTable.insert {
                it[id] = EntityID(ticker1Id, TickerTable)
                it[code] = "AAAA"
                it[fullCompanyName] = "Company A Inc."
                it[shortCompanyName] = "Company A"
                it[country] = "USA"
                it[marketCap] = 1000L
                it[category] = "Technology"
                it[exchangeCode] = "NASDAQ"
                it[createdAt] = LocalDateTime.now()
            }

            TickerTable.insert {
                it[id] = EntityID(ticker2Id, TickerTable)
                it[code] = "BBBB"
                it[fullCompanyName] = "Company B Inc."
                it[shortCompanyName] = "Company B"
                it[country] = "USA"
                it[marketCap] = 2000L
                it[category] = "Technology"
                it[exchangeCode] = "NASDAQ"
                it[createdAt] = LocalDateTime.now()
            }

            PredictionTable.insert {
                it[id] = EntityID(UUID.randomUUID(), PredictionTable)
                it[predictionDate] = latestDate
                it[score] = 70
                it[tickerId] = ticker1Id
                it[positiveArticleCount] = 10L
                it[negativeArticleCount] = 3L
                it[neutralArticleCount] = 5L
                it[sentiment] = 1
                it[strategy] = "STRONG_BUY"
                it[tickerCode] = "AAAA"
                it[shortCompanyName] = "Company A"
                it[createdAt] = LocalDateTime.now()
            }
            PredictionTable.insert {
                it[id] = EntityID(UUID.randomUUID(), PredictionTable)
                it[predictionDate] = latestDate.minusDays(1)
                it[score] = 80
                it[tickerId] = ticker1Id
                it[positiveArticleCount] = 10L
                it[negativeArticleCount] = 3L
                it[neutralArticleCount] = 5L
                it[sentiment] = 1
                it[strategy] = "STRONG_BUY"
                it[tickerCode] = "AAAA"
                it[shortCompanyName] = "Company A"
                it[createdAt] = LocalDateTime.now()
            }

            PredictionTable.insert {
                it[id] = EntityID(UUID.randomUUID(), PredictionTable)
                it[predictionDate] = latestDate
                it[score] = 90
                it[tickerId] = ticker2Id
                it[positiveArticleCount] = 15L
                it[negativeArticleCount] = 1L
                it[neutralArticleCount] = 2L
                it[sentiment] = 1
                it[strategy] = "STRONG_BUY"
                it[tickerCode] = "BBBB"
                it[shortCompanyName] = "Company B"
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

    Given("savePrediction 메서드는") {
        When("새로운 예측 데이터를 저장하면") {
            val newTickerId = UUID.randomUUID()
            val newPrediction = PredictionC.create(
                tickerId = newTickerId,
                tickerCode = "NEWCO",
                shortCompanyName = "New Company",
                positiveArticleCount = 5L,
                negativeArticleCount = 1L,
                neutralArticleCount = 2L,
                predictionDate = latestDate,
                todayScores = listOf(50, 55) // 예시 점수
            )

            Then("데이터가 성공적으로 저장되어야 한다") {
                val found = transaction {
                    predictionRepository.savePrediction(newPrediction)
                    PredictionExposed.find { PredictionTable.tickerId eq newTickerId }
                        .singleOrNull()
                }
                found shouldNotBe null
                found?.score shouldBe newPrediction.sentimentScore
                found?.strategy shouldBe newPrediction.predictionStrategy.strategy
            }
        }
    }

    Given("updatePrediction 메서드는") {
        When("기존 예측 데이터가 존재할 때 업데이트를 시도하면") {
            val predictionToUpdate = PredictionC.create(
                tickerId = ticker1Id, // beforeEach에서 생성된 ticker1
                tickerCode = "AAAA",
                shortCompanyName = "Company A",
                positiveArticleCount = 5L, // 추가할 뉴스 개수
                negativeArticleCount = 0L,
                neutralArticleCount = 0L,
                predictionDate = latestDate,
                todayScores = listOf(70) // 기존 점수
            )
            val updatedPrediction = transaction {
                predictionRepository.updatePrediction(predictionToUpdate)
            }
            Then("기존 데이터의 뉴스 개수가 누적되어 업데이트되어야 한다") {
                updatedPrediction shouldNotBe null
                // 기존 10L + 새로운 5L
                updatedPrediction.positiveArticleCount shouldBe 15L
                updatedPrediction.tickerId shouldBe ticker1Id
            }
        }
    }

    Given("getRecentSentimentScoreList 메서드는") {
        When("특정 티커 A의 최근 7일간 점수 조회를 요청하면") {
            val result = transaction {
                predictionRepository.getRecentSentimentScoreList(ticker1Id)
            }

            Then("해당 티커의 7일 이내 점수만 리스트로 반환해야 한다") {
                result shouldHaveSize 2
                result shouldContainExactlyInAnyOrder listOf(70, 80)
            }
        }
    }
})