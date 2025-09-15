package finn.repository.impl

import finn.TestApplication
import finn.entity.ArticleExposed
import finn.entity.ArticleTickerExposed
import finn.entity.TickerExposed
import finn.exception.CriticalDataPollutedException
import finn.repository.ArticleRepository
import finn.table.ArticleTable
import finn.table.TickerTable
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime

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
                country = "USA"
                marketCap = 100000L
                exchangeCode = "code"
                createdAt = LocalDateTime.now()
            }

            // 테스트에 사용할 Article 데이터 5개 생성
            val article1 = ArticleExposed.new {
                // ticker 객체 참조 대신 tickerId와 tickerCode를 직접 할당
                this.publishedDate = LocalDateTime.now().minusDays(1)
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
                this.title = article1.title
                this.sentiment = "positive"
                this.reasoning = "..."
                this.publishedDate = LocalDateTime.now().minusDays(1)
                this.createdAt = LocalDateTime.now()
            }

            val article2 = ArticleExposed.new {
                this.publishedDate = LocalDateTime.now().minusDays(2)
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
                this.title = article2.title
                this.sentiment = "negative"
                this.reasoning = "..."
                this.publishedDate = LocalDateTime.now().minusDays(2)
                this.createdAt = LocalDateTime.now()
            }

            val article3 = ArticleExposed.new {
                this.publishedDate = LocalDateTime.now().minusDays(3)
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
                this.title = article3.title
                this.sentiment = "positive"
                this.reasoning = "..."
                this.publishedDate = LocalDateTime.now().minusDays(3)
                this.createdAt = LocalDateTime.now()
            }

            val article4 = ArticleExposed.new {
                this.publishedDate = LocalDateTime.now().minusDays(4)
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
                this.title = article4.title
                this.sentiment = "negative"
                this.reasoning = "..."
                this.publishedDate = LocalDateTime.now().minusDays(4)
                this.createdAt = LocalDateTime.now()
            }

            val article5 = ArticleExposed.new {
                this.publishedDate = LocalDateTime.now().minusDays(5)
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
                this.title = article5.title
                this.sentiment = "neutral"
                this.reasoning = "..."
                this.publishedDate = LocalDateTime.now().minusDays(5)
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
//        When("filter가 'positive'일 때") {
//            val result = transaction {
//                articleRepository.getArticleList(
//                    page = 0,
//                    size = 10,
//                    filter = "positive",
//                    sort = "recent"
//                )
//            }
//
//            Then("긍정적인 뉴스(3개)만 최신순으로 반환해야 한다") {
//                result.content shouldHaveSize 3
//                result.content.all { it.sentiment == "positive" } shouldBe true
//                result.content[0].title shouldBe "가장 최신 긍정 뉴스"
//            }
//        }
//
//        When("filter가 'negative'일 때") {
//            val result = transaction {
//                articleRepository.getArticleList(
//                    page = 0,
//                    size = 10,
//                    filter = "negative",
//                    sort = "recent"
//                )
//            }
//
//            Then("부정적인 뉴스(2개)만 최신순으로 반환해야 한다") {
//                result.content shouldHaveSize 2
//                result.content.all { it.sentiment == "negative" } shouldBe true
//                result.content[0].title shouldBe "두 번째 최신 부정 뉴스"
//            }
//        }

        When("filter가 'all'이고 size가 3일 때") {
            val result = transaction {
                articleRepository.getArticleList(
                    page = 0,
                    size = 3,
                    filter = "all",
                    sort = "recent"
                )
            }
            Then("전체 뉴스 중 최신 3개를 반환하고, 다음 페이지가 있음을 알려줘야 한다") {
                result.content shouldHaveSize 3
                result.hasNext shouldBe true
                result.content[0].title shouldBe "가장 최신 긍정 뉴스"
            }
        }

        When("filter가 지원하지 않는 옵션일 때") {
            val invalidFilter = "unsupported_option"

            Then("ServerErrorCriticalDataPollutedException 예외가 발생해야 한다") {
                // shouldThrow 블록 안에서 예외가 발생하면 테스트 성공
                shouldThrow<CriticalDataPollutedException> {
                    transaction {
                        articleRepository.getArticleList(
                            page = 0,
                            size = 10,
                            filter = invalidFilter,
                            sort = "recent"
                        )
                    }
                }
            }
        }
    }
})