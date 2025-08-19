package finn.repository.impl

import finn.TestApplication
import finn.entity.NewsExposed
import finn.entity.TickerExposed
import finn.repository.NewsRepository
import finn.table.NewsTable
import finn.table.TickerTable
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime

@SpringBootTest(classes = [TestApplication::class])
internal class NewsRepositoryImplTest(
    private val newsRepository: NewsRepository
) : BehaviorSpec({

    lateinit var ticker: TickerExposed

    beforeTest {
        transaction {
            // 자식 테이블인 NewsTable을 먼저 삭제
            NewsTable.deleteAll()
            TickerTable.deleteAll()

            ticker = TickerExposed.new {
                code = "TEST"
                fullCompanyName = "Test Company Inc."
                shortCompanyName = "Test"
                country = "USA"
                marketCap = 100000L
                createdAt = LocalDateTime.now()
            }

            // 테스트에 사용할 News 데이터 5개 생성
            NewsExposed.new {
                // ticker 객체 참조 대신 tickerId와 tickerCode를 직접 할당
                this.tickerId = ticker.id.value
                this.tickerCode = ticker.code
                this.publishedDate = LocalDateTime.now().minusDays(1)
                this.title = "가장 최신 긍정 뉴스"
                this.description = "긍정적인 내용입니다."
                this.contentUrl = "https://news.com/1"
                this.sentiment = "positive"
                this.shortCompanyName = ticker.shortCompanyName
                this.author = "Reuters"
                this.distinctId = "news-1"
                this.createdAt = LocalDateTime.now()
            }
            NewsExposed.new {
                this.tickerId = ticker.id.value
                this.tickerCode = ticker.code
                this.publishedDate = LocalDateTime.now().minusDays(2)
                this.title = "두 번째 최신 부정 뉴스"
                this.description = "부정적인 내용입니다."
                this.contentUrl = "https://news.com/2"
                this.sentiment = "negative"
                this.shortCompanyName = ticker.shortCompanyName
                this.author = "AP"
                this.distinctId = "news-2"
                this.createdAt = LocalDateTime.now()
            }
            NewsExposed.new {
                this.tickerId = ticker.id.value
                this.tickerCode = ticker.code
                this.publishedDate = LocalDateTime.now().minusDays(3)
                this.title = "세 번째 최신 긍정 뉴스"
                this.description = "긍정적인 내용입니다."
                this.contentUrl = "https://news.com/3"
                this.sentiment = "positive"
                this.shortCompanyName = ticker.shortCompanyName
                this.author = "AP"
                this.distinctId = "news-3"
                this.createdAt = LocalDateTime.now()
            }
            NewsExposed.new {
                this.tickerId = ticker.id.value
                this.tickerCode = ticker.code
                this.publishedDate = LocalDateTime.now().minusDays(4)
                this.title = "네 번째 최신 부정 뉴스"
                this.description = "부정적인 내용입니다."
                this.contentUrl = "https://news.com/4"
                this.sentiment = "positive"
                this.shortCompanyName = ticker.shortCompanyName
                this.author = "AP"
                this.distinctId = "news-4"
                this.createdAt = LocalDateTime.now()
            }
            NewsExposed.new {
                this.tickerId = ticker.id.value
                this.tickerCode = ticker.code
                this.publishedDate = LocalDateTime.now().minusDays(5)
                this.title = "가장 오래된 부정 뉴스"
                this.description = "부정적인 내용입니다."
                this.contentUrl = "https://news.com/5"
                this.sentiment = "negative"
                this.shortCompanyName = ticker.shortCompanyName
                this.author = "AP"
                this.distinctId = "news-5"
                this.createdAt = LocalDateTime.now()
            }
        }
    }

    Context("getNewsDataForPredictionDetail 메서드는") {
        When("특정 tickerId로 호출되면") {
            val result = transaction {
                newsRepository.getNewsDataForPredictionDetail(ticker.id.value)
            }
            Then("해당 ticker의 가장 최신 뉴스 3개를 반환해야 한다") {
                result shouldHaveSize 3
                result[0].headline() shouldBe "가장 최신 긍정 뉴스"
                result[1].headline() shouldBe "두 번째 최신 부정 뉴스"
                result[2].headline() shouldBe "세 번째 최신 긍정 뉴스"
            }
        }
    }

    Context("getNewsList 메서드는") {
        When("filter가 'positive'일 때") {
            val result = transaction {
                newsRepository.getNewsList(
                    page = 0,
                    size = 10,
                    filter = "positive",
                    sort = "latest"
                )
            }

            Then("긍정적인 뉴스(3개)만 최신순으로 반환해야 한다") {
                result.content shouldHaveSize 3
                result.content.all { it.sentiment == "positive" } shouldBe true
                result.content[0].title shouldBe "가장 최신 긍정 뉴스"
            }
        }

        When("filter가 'negative'일 때") {
            val result = transaction {
                newsRepository.getNewsList(
                    page = 0,
                    size = 10,
                    filter = "negative",
                    sort = "latest"
                )
            }

            Then("부정적인 뉴스(2개)만 최신순으로 반환해야 한다") {
                result.content shouldHaveSize 2
                result.content.all { it.sentiment == "negative" } shouldBe true
                result.content[0].title shouldBe "두 번째 최신 부정 뉴스"
            }
        }

        When("filter가 'all'이고 size가 3일 때") {
            val result = transaction {
                newsRepository.getNewsList(page = 0, size = 3, filter = "all", sort = "latest")
            }
            Then("전체 뉴스 중 최신 3개를 반환하고, 다음 페이지가 있음을 알려줘야 한다") {
                result.content shouldHaveSize 3
                result.hasNext shouldBe true
                result.content[0].title shouldBe "가장 최신 긍정 뉴스"
            }
        }
    }
})