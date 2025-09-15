package finn.repository.impl

import finn.TestApplication
import finn.entity.MarketStatusExposed
import finn.repository.MarketStatusRepository
import finn.table.MarketStatusTable
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate

@SpringBootTest(classes = [TestApplication::class])
internal class MarketStatusRepositoryImplTest(
    private val marketStatusRepository: MarketStatusRepository
) : BehaviorSpec({

    Given("데이터베이스에 특정 날짜의 시장 상태 정보가 존재할 때") {
        val holidayDate = LocalDate.of(2025, 8, 15)

        // 테스트 시작 전 DB를 초기화하고 테스트 데이터를 삽입
        beforeTest {
            transaction {
                MarketStatusTable.deleteAll()
                MarketStatusExposed.new {
                    date = holidayDate
                    tradingHours = "휴장"
                    eventName = "Independence Day"
                }
            }
        }

        When("해당 날짜로 getOptionalMarketStatus를 호출하면") {
            val result = transaction {
                marketStatusRepository.getOptionalMarketStatus(holidayDate)
            }

            Then("null이 아닌 MarketStatus 객체를 반환해야 한다") {
                result.shouldNotBeNull()
                result.date shouldBe holidayDate
                result.tradingHours shouldBe "휴장"
                result.eventName shouldBe "Independence Day"
            }
        }
    }

    Given("데이터베이스에 특정 날짜의 시장 상태 정보가 존재하지 않을 때") {
        val businessDay = LocalDate.of(2025, 8, 18)

        beforeTest {
            transaction {
                MarketStatusTable.deleteAll()
            }
        }

        When("해당 날짜로 getOptionalMarketStatus를 호출하면") {
            val result = transaction {
                marketStatusRepository.getOptionalMarketStatus(businessDay)
            }

            Then("null을 반환해야 한다") {
                result.shouldBeNull()
            }
        }
    }
})