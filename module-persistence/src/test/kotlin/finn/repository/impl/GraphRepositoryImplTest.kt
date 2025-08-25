package finn.repository.impl

import finn.TestApplication
import finn.exception.CriticalDataOmittedException
import finn.repository.GraphRepository
import finn.table.NIntervalChangeRateTable
import finn.table.TickerPriceTable
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@SpringBootTest(classes = [TestApplication::class])
internal class GraphRepositoryImplTest(
    private val graphRepository: GraphRepository
) : BehaviorSpec({

    fun setupGraphData(tickerId: UUID, startDate: LocalDate, days: Int) {
        transaction {
            (0 until days).forEach { i ->
                val currentDate = startDate.plusDays(i.toLong())
                TickerPriceTable.insert {
                    it[TickerPriceTable.id] = UUID.randomUUID()
                    it[TickerPriceTable.tickerId] = tickerId
                    it[TickerPriceTable.tickerCode] = "TEST"
                    it[TickerPriceTable.priceDate] = currentDate
                    it[TickerPriceTable.close] = (1000 + i).toBigDecimal()
                    it[TickerPriceTable.open] = (1000 + i).toBigDecimal()
                    it[TickerPriceTable.high] = (1000 + i).toBigDecimal()
                    it[TickerPriceTable.low] = (1000 + i).toBigDecimal()
                    it[TickerPriceTable.volume] = 100000L
                    it[TickerPriceTable.createdAt] = LocalDateTime.now()
                }
                (intArrayOf(1, 7)).forEach { j ->
                    NIntervalChangeRateTable.insert {
                        it[NIntervalChangeRateTable.id] = UUID.randomUUID()
                        it[NIntervalChangeRateTable.tickerId] = tickerId
                        it[NIntervalChangeRateTable.tickerCode] = "TEST"
                        it[NIntervalChangeRateTable.priceDate] = currentDate
                        it[NIntervalChangeRateTable.interval] = j
                        it[NIntervalChangeRateTable.changeRate] = i.toBigDecimal()
                        it[NIntervalChangeRateTable.createdAt] = LocalDateTime.now()
                    }
                }
            }
        }
    }

    // 각 테스트 시작 전 DB 초기화
    beforeTest {
        transaction {
            NIntervalChangeRateTable.deleteAll()
            TickerPriceTable.deleteAll()
        }
    }

    // --- 테스트 시나리오 ---

    Given("조회 범위 내에 데이터가 전혀 없을 때") {
        // Ticker를 직접 생성하는 대신 UUID만 생성
        val tickerId = UUID.randomUUID()
        val startDate = LocalDate.of(2025, 1, 1)
        val endDate = LocalDate.of(2025, 1, 31)

        When("getTickerGraph를 호출하면") {
            Then("ServerErrorCriticalDataOmittedException 예외가 발생해야 한다") {
                shouldThrow<CriticalDataOmittedException> {
                    transaction {
                        graphRepository.getTickerGraph(tickerId, startDate, endDate, 7, 10)
                    }
                }
            }
        }
    }

    Given("데이터 개수가 minimumCount보다 적을 때 (5개 < 10개)") {
        // Ticker를 직접 생성하는 대신 UUID만 생성
        val tickerId = UUID.randomUUID()
        val startDate = LocalDate.of(2025, 8, 1)
        val endDate = startDate.plusDays(10)

        When("minimumCount=10, interval=1로 getTickerGraph를 호출하면") {
            val result = transaction {
                setupGraphData(tickerId, startDate, 5) // 2025-08-01 ~ 2025-08-05
                graphRepository.getTickerGraph(tickerId, startDate, endDate, 1, 10)
            }

            Then("interval을 무시하고 모든 데이터(5개)를 반환해야 한다") {
                result shouldHaveSize 5
            }
        }
    }

    Given("데이터 개수가 minimumCount보다 많을 때 (10개 > 5개)") {
        // Ticker를 직접 생성하는 대신 UUID만 생성
        val tickerId = UUID.randomUUID()
        val startDate = LocalDate.of(2025, 8, 1)
        val endDate = startDate.plusDays(10)

        When("minimumCount=5, interval=1로 getTickerGraph를 호출하면") {
            val result = transaction {
                setupGraphData(tickerId, startDate, 10) // 2025-08-01 ~ 2025-08-10(1, 4, 7, 10)
                graphRepository.getTickerGraph(tickerId, startDate, endDate, 1, 5)
            }

            Then("interval에 맞는 데이터와 마지막 날짜의 데이터를 포함하여 10개를 반환해야 한다") {
                result shouldHaveSize 10

                val resultDates = result.map { it.date() }
                resultDates shouldContainExactlyInAnyOrder listOf(
                    LocalDate.of(2025, 8, 1),
                    LocalDate.of(2025, 8, 2),
                    LocalDate.of(2025, 8, 3),
                    LocalDate.of(2025, 8, 4),
                    LocalDate.of(2025, 8, 5),
                    LocalDate.of(2025, 8, 6),
                    LocalDate.of(2025, 8, 7),
                    LocalDate.of(2025, 8, 8),
                    LocalDate.of(2025, 8, 9),
                    LocalDate.of(2025, 8, 10)
                )
            }
        }
    }
})