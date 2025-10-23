import finn.TestApplication
import finn.exception.CriticalDataOmittedException
import finn.exception.CriticalDataPollutedException
import finn.repository.PredictionRepository
import finn.table.PredictionTable
import finn.table.TickerTable
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
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
                it[shortCompanyNameKr] = "A 회사"
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
                it[shortCompanyNameKr] = "B 회사"
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
                it[volatility] = BigDecimal.ZERO
                it[strategy] = "강한 호재"
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
                it[volatility] = BigDecimal.ZERO
                it[strategy] = "강한 호재"
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
                it[volatility] = BigDecimal.ZERO
                it[strategy] = "강한 호재"
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

    Given("updatePredictionByArticle 메서드는") {
        When("존재하는 예측 데이터에 업데이트를 요청하면") {
            // 기존 데이터: positive=10, negative=3, neutral=5, score=70
            val newPositiveCount = 5L
            val newNegativeCount = 2L
            val newNeutralCount = 1L
            val newScore = 75

            // 업데이트 실행
            val updatedPrediction = newSuspendedTransaction {
                predictionRepository.updatePredictionByArticle(
                    tickerId = ticker1Id,
                    predictionDate = latestDate,
                    positiveArticleCount = newPositiveCount,
                    negativeArticleCount = newNegativeCount,
                    neutralArticleCount = newNeutralCount,
                    score = newScore,
                    sentiment = 1,
                    strategy = "약한 호재"
                )
            }

            Then("기사 개수는 누적되고 점수는 갱신되어야 한다") {
                updatedPrediction.tickerId shouldBe ticker1Id

                // 기사 개수는 기존 값에 새로운 값이 더해져야 함
                updatedPrediction.positiveArticleCount shouldBe 10L + newPositiveCount // 15
                updatedPrediction.negativeArticleCount shouldBe 3L + newNegativeCount // 5
                updatedPrediction.neutralArticleCount shouldBe 5L + newNeutralCount   // 6

                // 점수는 새로운 값으로 덮어쓰기 되어야 함
                updatedPrediction.sentimentScore shouldBe newScore // 75
            }
        }

        When("존재하지 않는 예측 데이터에 업데이트를 요청하면") {
            val nonExistentTickerId = UUID.randomUUID()

            Then("CriticalDataOmittedException 예외가 발생해야 한다") {
                    newSuspendedTransaction {
                        shouldThrow<CriticalDataOmittedException> {
                        predictionRepository.updatePredictionByArticle(
                            tickerId = nonExistentTickerId,
                            predictionDate = latestDate,
                            positiveArticleCount = 1L,
                            negativeArticleCount = 1L,
                            neutralArticleCount = 1L,
                            score = 50,
                            sentiment = 0,
                            strategy = "관망"
                        )
                    }
                }
            }
        }
    }

    Given("getRecentSentimentScoreList 메서드는") {
        When("특정 티커 A의 최근 7일간 점수 조회를 요청하면") {
            val result = newSuspendedTransaction {
                predictionRepository.getRecentSentimentScoreList(ticker1Id)
            }

            Then("해당 티커의 7일 이내 점수만 리스트로 반환해야 한다") {
                result shouldHaveSize 2
                result shouldContainExactlyInAnyOrder listOf(70, 80)
            }
        }
    }
})