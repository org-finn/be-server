package finn.repository.impl

import finn.TestApplication
import finn.entity.ArticleExposed
import finn.entity.ArticleTickerExposed
import finn.entity.TickerExposed
import finn.repository.ArticleRepository
import finn.table.ArticleTable
import finn.table.TickerTable
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@SpringBootTest(classes = [TestApplication::class])
internal class ArticleRepositoryImplTest(
    private val articleRepository: ArticleRepository
) : BehaviorSpec({

    lateinit var ticker: TickerExposed

    beforeTest {
        transaction {
            // 자식 테이블인 ArticleTable을 먼저 삭제
            ArticleTable.deleteAll()
            TickerTable.deleteAll()

            ticker = TickerExposed.new {
                code = "TEST"
                fullCompanyName = "Test Company Inc."
                shortCompanyName = "Test"
                shortCompanyNameKr = "테스트"
                country = "USA"
                marketCap = 100000L
                exchangeCode = "code"
                createdAt = LocalDateTime.now()
            }

            // 테스트에 사용할 Article 데이터 5개 생성
            val article1 = ArticleExposed.new {
                // ticker 객체 참조 대신 tickerId와 tickerCode를 직접 할당
                this.publishedDate = Instant.now().minus(1, ChronoUnit.DAYS)
                this.title = "가장 최신 긍정 뉴스"
                this.description = "긍정적인 내용입니다."
                this.contentUrl = "https://Article.com/1"
                this.author = "Reuters"
                this.distinctId = "Article-1"
                this.createdAt = LocalDateTime.now()
            }
            ArticleTickerExposed.new {
                this.articleId = article1.id.value
                this.tickerId = ticker.id.value
                this.tickerCode = ticker.code
                this.shortCompanyName = ticker.shortCompanyName
                this.title = article1.title
                this.sentiment = "positive"
                this.reasoning = "..."
                this.publishedDate = Instant.now().minus(1, ChronoUnit.DAYS)
                this.createdAt = LocalDateTime.now()
            }

            val article2 = ArticleExposed.new {
                this.publishedDate = Instant.now().minus(2, ChronoUnit.DAYS)
                this.title = "두 번째 최신 부정 뉴스"
                this.description = "부정적인 내용입니다."
                this.contentUrl = "https://Article.com/2"
                this.author = "AP"
                this.distinctId = "Article-2"
                this.createdAt = LocalDateTime.now()
            }
            ArticleTickerExposed.new {
                this.articleId = article2.id.value
                this.tickerId = ticker.id.value
                this.tickerCode = ticker.code
                this.shortCompanyName = ticker.shortCompanyName
                this.title = article2.title
                this.sentiment = "negative"
                this.reasoning = "..."
                this.publishedDate = Instant.now().minus(2, ChronoUnit.DAYS)
                this.createdAt = LocalDateTime.now()
            }

            val article3 = ArticleExposed.new {
                this.publishedDate = Instant.now().minus(3, ChronoUnit.DAYS)
                this.title = "세 번째 최신 긍정 뉴스"
                this.description = "긍정적인 내용입니다."
                this.contentUrl = "https://Article.com/3"
                this.author = "AP"
                this.distinctId = "Article-3"
                this.createdAt = LocalDateTime.now()
            }
            ArticleTickerExposed.new {
                this.articleId = article3.id.value
                this.tickerId = ticker.id.value
                this.tickerCode = ticker.code
                this.shortCompanyName = ticker.shortCompanyName
                this.title = article3.title
                this.sentiment = "positive"
                this.reasoning = "..."
                this.publishedDate = Instant.now().minus(3, ChronoUnit.DAYS)
                this.createdAt = LocalDateTime.now()
            }

            val article4 = ArticleExposed.new {
                this.publishedDate = Instant.now().minus(4, ChronoUnit.DAYS)
                this.title = "네 번째 최신 부정 뉴스"
                this.description = "부정적인 내용입니다."
                this.contentUrl = "https://Article.com/4"
                this.author = "AP"
                this.distinctId = "Article-4"
                this.createdAt = LocalDateTime.now()
            }
            ArticleTickerExposed.new {
                this.articleId = article4.id.value
                this.tickerId = ticker.id.value
                this.tickerCode = ticker.code
                this.shortCompanyName = ticker.shortCompanyName
                this.title = article4.title
                this.sentiment = "negative"
                this.reasoning = "..."
                this.publishedDate = Instant.now().minus(4, ChronoUnit.DAYS)
                this.createdAt = LocalDateTime.now()
            }

            val article5 = ArticleExposed.new {
                this.publishedDate = Instant.now().minus(5, ChronoUnit.DAYS)
                this.title = "가장 오래된 중립 뉴스"
                this.description = "중립적인 내용입니다."
                this.contentUrl = "https://Article.com/5"
                this.author = "AP"
                this.distinctId = "Article-5"
                this.createdAt = LocalDateTime.now()
            }
            ArticleTickerExposed.new {
                this.articleId = article5.id.value
                this.tickerId = ticker.id.value
                this.tickerCode = ticker.code
                this.shortCompanyName = ticker.shortCompanyName
                this.title = article5.title
                this.sentiment = "neutral"
                this.reasoning = "..."
                this.publishedDate = Instant.now().minus(5, ChronoUnit.DAYS)
                this.createdAt = LocalDateTime.now()
            }

        }
    }

    Context("getArticleDataForPredictionDetail 메서드는") {
        When("특정 tickerId로 호출되면") {
            val result = transaction {
                articleRepository.getArticleDataForPredictionDetail(ticker.id.value)
            }
            Then("해당 ticker의 가장 최신 뉴스 3개를 반환해야 한다") {
                result shouldHaveSize 3
                result[0].headline() shouldBe "가장 최신 긍정 뉴스"
                result[1].headline() shouldBe "두 번째 최신 부정 뉴스"
                result[2].headline() shouldBe "세 번째 최신 긍정 뉴스"
            }
        }
    }

    Context("getArticleList 메서드는") {
        When("filter가 'all'이고 size가 3일 때") {
            val result = transaction {
                articleRepository.getArticleList(
                    page = 0,
                    size = 3,
                    tickerCodes = null,
                    sentiment = null,
                    sort = "recent"
                )
            }
            Then("전체 뉴스 중 최신 3개를 반환하고, 다음 페이지가 있음을 알려줘야 한다") {
                result.content shouldHaveSize 3
                result.hasNext shouldBe true
                result.content[0].title shouldBe "가장 최신 긍정 뉴스"
            }
        }

        When("tickerCodes로 필터링하면") {
            val result = transaction {
                articleRepository.getArticleList(
                    page = 0,
                    size = 10,
                    tickerCodes = listOf(ticker.code),
                    sentiment = null,
                    sort = "recent"
                )
            }
            Then("해당 tickerCodes를 가진 뉴스만을 반환해야 한다") {
                result.content shouldHaveSize 5
                result.content[0].tickers?.contains(ticker.shortCompanyName)
            }
        }

        When("sentiment가 'positive'로 필터링하면") {
            val result = transaction {
                articleRepository.getArticleList(
                    page = 0,
                    size = 10,
                    tickerCodes = null,
                    sentiment = "positive",
                    sort = "recent"
                )
            }
            Then("sentiment가 'positive'인 뉴스만 반환해야 한다") {
                result.content shouldHaveSize 2
                result.content[0].title shouldBe "가장 최신 긍정 뉴스"
                result.content[1].title shouldBe "세 번째 최신 긍정 뉴스"
            }
        }

        When("tickerCodes와 sentiment 모두로 필터링하면") {
            val result = transaction {
                articleRepository.getArticleList(
                    page = 0,
                    size = 10,
                    tickerCodes = listOf(ticker.code),
                    sentiment = "negative",
                    sort = "recent"
                )
            }
            Then("두 조건을 모두 만족하는 뉴스만 반환해야 한다") {
                result.content shouldHaveSize 2
                result.content[0].tickers?.contains(ticker.shortCompanyName)
                result.content[0].title shouldBe "두 번째 최신 부정 뉴스"
                result.content[1].title shouldBe "네 번째 최신 부정 뉴스"
            }
        }

        When("존재하지 않는 tickerCode로 필터링하면") {
            val result = transaction {
                articleRepository.getArticleList(
                    page = 0,
                    size = 10,
                    tickerCodes = listOf("NOT_EXISTED_CODE"),
                    sentiment = null,
                    sort = "recent"
                )
            }
            Then("빈 리스트가 반환되어야 한다") {
                result.content.isEmpty() shouldBe true
            }
        }
    }
})