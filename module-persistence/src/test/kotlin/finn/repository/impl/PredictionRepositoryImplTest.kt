import finn.TestApplication
import finn.exception.CriticalDataOmittedException
import finn.exception.CriticalDataPollutedException
import finn.repository.PredictionRepository
import finn.table.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@SpringBootTest(classes = [TestApplication::class])
internal class PredictionRepositoryImplTest(
    private val predictionRepository: PredictionRepository
) : BehaviorSpec({

    val ticker1Id = UUID.randomUUID()
    val ticker2Id = UUID.randomUUID()
    val latestDate = LocalDateTime.now(ZoneId.of("UTC"))

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
                predictionRepository.getPredictionList(
                    page = 0,
                    size = 10,
                    sort = "popular",
                    param = null
                )
            }

            Then("시가총액(marketCap)이 높은 순으로 정렬되어야 한다") {
                result.content.size shouldBe 2
                result.content[0].tickerCode shouldBe "BBBB" // marketCap 2000
                result.content[1].tickerCode shouldBe "AAAA" // marketCap 1000
            }
        }

        When("sort 파라미터가 'upward'일 때") {
            val result = transaction {
                predictionRepository.getPredictionList(
                    page = 0,
                    size = 10,
                    sort = "upward",
                    param = null
                )
            }

            Then("점수(score)가 낮은 순(오름차순)으로 정렬되어야 한다") {
                result.content[0].tickerCode shouldBe "AAAA"
                result.content[1].tickerCode shouldBe "BBBB"
            }
        }

        When("sort 파라미터가 'downward'일 때") {
            val result = transaction {
                predictionRepository.getPredictionList(
                    page = 0,
                    size = 10,
                    sort = "downward",
                    param = null
                )
            }

            Then("점수(score)가 높은 순(내림차순)으로 정렬되어야 한다") {
                result.content[0].tickerCode shouldBe "BBBB"
                result.content[1].tickerCode shouldBe "AAAA"
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
                            sort = invalidSort,
                            param = null
                        )
                    }
                }
            }
        }

        When("param 파라미터가 'keyword'일 때") {
            val keywordParam = "keyword"

            // 키워드 데이터 준비
            transaction {
                // 테스트 격리를 위해 관련 테이블 초기화 (필요시)
                ArticleSummaryTable.deleteAll()

                ArticleSummaryTable.insert {
                    it[id] = EntityID(UUID.randomUUID(), ArticleSummaryTable)
                    it[tickerId] = ticker1Id
                    it[positiveKeywords] = "호재,상승,기대"
                    it[negativeKeywords] = "우려,하락"
                    it[shortCompanyName] = "Company B"
                    it[summaryDate] = LocalDateTime.now(ZoneId.of("UTC")) // 쿼리 조건: 오늘 날짜
                    it[createdAt] = LocalDateTime.now()
                }
            }

            val result = transaction {
                predictionRepository.getPredictionList(
                    page = 0,
                    size = 10,
                    sort = "popular",
                    param = keywordParam
                )
            }

            Then("해당 티커의 긍정/부정 키워드 데이터가 포함되어야 한다") {
                val targetItem = result.content.find { it.tickerId == ticker1Id }
                targetItem.shouldNotBeNull()

                targetItem.positiveKeywords shouldBe "호재,상승,기대"
                targetItem.negativeKeywords shouldBe "우려,하락"

                // 데이터가 없는 다른 티커는 null이어야 함
                val otherItem = result.content.find { it.tickerId == ticker2Id }
                otherItem?.positiveKeywords.shouldBeNull()
            }
        }

        When("param 파라미터가 'article'일 때") {
            val articleParam = "article"

            val article1Id = UUID.randomUUID()
            val article1Title = "Company A 3분기 실적 발표"

            // 기사 데이터 준비
            transaction {
                ArticleTickerTable.deleteAll()

                ArticleTickerTable.insert {
                    it[id] = EntityID(UUID.randomUUID(), ArticleTickerTable)
                    it[articleId] = article1Id
                    it[tickerId] = ticker1Id
                    it[tickerCode] = "tickerCode"
                    it[shortCompanyName] = "Company B"
                    it[title] = article1Title
                    it[titleKr] = article1Title
                    it[publishedDate] = Instant.now(Clock.system(ZoneId.of("UTC"))) // 쿼리 조건: 오늘 날짜
                    it[createdAt] = LocalDateTime.now()
                }
            }

            val result = transaction {
                predictionRepository.getPredictionList(
                    page = 0,
                    size = 10,
                    sort = "popular",
                    param = articleParam
                )
            }

            Then("해당 티커의 관련 기사 제목 리스트가 포함되어야 한다") {
                val targetItem = result.content.find { it.tickerId == ticker1Id }
                targetItem.shouldNotBeNull()
                targetItem.articleTitles.shouldNotBeNull()

                val articles = targetItem.articleTitles!!
                articles shouldHaveSize 1
                articles[0].title shouldBe article1Title
                articles[0].articleId shouldBe article1Id
            }
        }

        When("param 파라미터가 'graph'일 때") {
            val graphParam = "graph"

            // 주가 데이터 준비 (최근 15일 이내 데이터)
            transaction {
                TickerPriceTable.deleteAll()

                // 어제 주가
                TickerPriceTable.insert {
                    it[id] = EntityID(UUID.randomUUID(), TickerPriceTable)
                    it[tickerId] = ticker1Id
                    it[tickerCode] = "tickerCode"
                    it[close] = BigDecimal("150.00")
                    it[priceDate] = LocalDateTime.now().minusDays(1)
                    it[changeRate] = BigDecimal.ZERO
                    it[open] = BigDecimal.ZERO; it[high] = BigDecimal.ZERO; it[low] =
                    BigDecimal.ZERO; it[volume] = 0L
                    it[createdAt] = LocalDateTime.now()
                }
                // 3일 전 주가
                TickerPriceTable.insert {
                    it[id] = EntityID(UUID.randomUUID(), TickerPriceTable)
                    it[tickerId] = ticker1Id
                    it[tickerCode] = "tickerCode"
                    it[close] = BigDecimal("145.00")
                    it[priceDate] = LocalDateTime.now().minusDays(3)
                    it[changeRate] = BigDecimal.ZERO
                    it[open] = BigDecimal.ZERO; it[high] = BigDecimal.ZERO; it[low] =
                    BigDecimal.ZERO; it[volume] = 0L
                    it[createdAt] = LocalDateTime.now()
                }
            }

            val result = transaction {
                predictionRepository.getPredictionList(
                    page = 0,
                    size = 10,
                    sort = "popular",
                    param = graphParam
                )
            }

            Then("해당 티커의 종가 데이터(close) 리스트가 포함되어야 한다") {
                val targetItem = result.content.find { it.tickerId == ticker1Id }
                targetItem.shouldNotBeNull()
                targetItem.graphData.shouldNotBeNull()

                val priceData = targetItem.graphData!!.priceData
                priceData shouldHaveSize 2

                // 정렬 조건 확인 (쿼리가 날짜 내림차순인지 확인)
                // Repository 쿼리: orderBy(TickerPriceTable.priceDate, SortOrder.DESC)
                priceData[0] shouldBe BigDecimal("150.0000") // 어제 (최신)
                priceData[1] shouldBe BigDecimal("145.0000") // 3일 전
            }
        }

        When("param 파라미터가 지원하지 않는 값일 때") {
            val invalidParam = "invalid_param_type"

            Then("CriticalDataPollutedException 예외가 발생해야 한다") {
                shouldThrow<CriticalDataPollutedException> {
                    transaction {
                        predictionRepository.getPredictionList(
                            page = 0, size = 10, sort = "popular", param = invalidParam
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