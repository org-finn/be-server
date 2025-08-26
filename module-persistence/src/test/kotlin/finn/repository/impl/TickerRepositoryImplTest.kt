package finn.repository.impl

import finn.TestApplication
import finn.repository.TickerRepository
import finn.table.TickerTable
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.util.*

@SpringBootTest(classes = [TestApplication::class])
internal class TickerRepositoryImplTest(
    private val tickerRepository: TickerRepository
) : BehaviorSpec({

    beforeTest {
        transaction {
            TickerTable.deleteAll()

            // 미국 기업 테스트 데이터 삽입
            TickerTable.insert {
                it[id] = UUID.randomUUID()
                it[code] = "AAPL"
                it[fullCompanyName] = "Apple Inc."
                it[shortCompanyName] = "Apple"
                it[country] = "USA"
                it[exchangeCode] = "CODE"
                it[createdAt] = LocalDateTime.now()
            }
            TickerTable.insert {
                it[id] = UUID.randomUUID()
                it[code] = "AMZN"
                it[fullCompanyName] = "Amazon.com Inc."
                it[shortCompanyName] = "Amazon"
                it[country] = "USA"
                it[exchangeCode] = "CODE"
                it[createdAt] = LocalDateTime.now()
            }
            TickerTable.insert {
                it[id] = UUID.randomUUID()
                it[code] = "MSFT"
                it[fullCompanyName] = "Microsoft Corporation"
                it[shortCompanyName] = "Microsoft"
                it[country] = "USA"
                it[exchangeCode] = "CODE"
                it[createdAt] = LocalDateTime.now()
            }
        }
    }

    Context("getTickerListBySearchKeyword 메서드는") {

        When("'A'로 검색하면") {
            val keyword = "A"
            val result = transaction {
                tickerRepository.getTickerListBySearchKeyword(keyword)
            }

            Then("'A'로 시작하는 Ticker 두 개(Apple, Amazon)를 반환해야 한다") {
                result shouldHaveSize 2
                result.any { it.shortCompanyName() == "Apple" } shouldBe true
                result.any { it.shortCompanyName() == "Amazon" } shouldBe true
            }
        }

        When("'Micro'로 검색하면") {
            val keyword = "Micro"
            val result = transaction {
                tickerRepository.getTickerListBySearchKeyword(keyword)
            }

            Then("'Micro'로 시작하는 Ticker 한 개(Microsoft)를 반환해야 한다") {
                result shouldHaveSize 1
                result.first().shortCompanyName() shouldBe "Microsoft"
            }
        }

        When("'Meta'로 검색하면") {
            val keyword = "Meta"
            val result = transaction {
                tickerRepository.getTickerListBySearchKeyword(keyword)
            }

            Then("결과가 없는 빈 리스트를 반환해야 한다") {
                result.shouldBeEmpty()
            }
        }
    }
})
