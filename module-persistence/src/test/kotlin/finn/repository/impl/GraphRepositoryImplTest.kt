package finn.repository.impl

import finn.TestApplication
import finn.exception.CriticalDataPollutedException
import finn.repository.GraphRepository
import finn.table.TickerPriceTable
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.test.context.SpringBootTest
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@SpringBootTest(classes = [TestApplication::class])
internal class GraphRepositoryImplTest(
    private val graphRepository: GraphRepository
) : BehaviorSpec({

    fun setupGraphData(tickerId: UUID, startDate: LocalDate, days: Int, holidays: Set<LocalDate>) {
        transaction {
            (0 until days).forEach { i ->
                val currentDate = startDate.plusDays(i.toLong())

                // 주말 또는 휴일인지 확인
                val isWeekend =
                    currentDate.dayOfWeek == DayOfWeek.SATURDAY || currentDate.dayOfWeek == DayOfWeek.SUNDAY
                val isHoliday = holidays.contains(currentDate)

                // 영업일에만 데이터를 삽입
                if (!isWeekend && !isHoliday) {
                    TickerPriceTable.insert {
                        it[TickerPriceTable.id] = UUID.randomUUID()
                        it[TickerPriceTable.tickerId] = tickerId
                        it[TickerPriceTable.tickerCode] = "TEST"
                        it[TickerPriceTable.priceDate] = currentDate.atStartOfDay()
                        it[TickerPriceTable.close] = (1000 + i).toBigDecimal()
                        it[TickerPriceTable.open] = (1000 + i).toBigDecimal()
                        it[TickerPriceTable.high] = (1000 + i).toBigDecimal()
                        it[TickerPriceTable.low] = (1000 + i).toBigDecimal()
                        it[TickerPriceTable.volume] = 100000L
                        it[TickerPriceTable.changeRate] = (10 + i).toBigDecimal()
                        it[TickerPriceTable.createdAt] = LocalDateTime.now()
                    }
                }
            }
        }
    }

    // 각 테스트 시작 전 DB 초기화
    beforeTest {
        transaction {
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
            Then("CriticalDataPollutedException 예외가 발생해야 한다") {
                shouldThrow<CriticalDataPollutedException> {
                    transaction {
                        graphRepository.getTickerGraph(tickerId, startDate, endDate, 7)
                    }
                }
            }
        }
    }

    Given("1일 간격으로 조회할 때") {
        val tickerId = UUID.randomUUID()
        val startDate = LocalDate.of(2025, 8, 18) // 월요일
        val endDate = LocalDate.of(2025, 8, 24)   // 일요일
        // 8/20(수)를 휴장일로 설정
        val holidays = setOf(LocalDate.of(2025, 8, 20))

        When("getTickerGraph를 호출하면") {
            val result = transaction {
                // 8/18 ~ 8/24 범위의 데이터 생성
                setupGraphData(tickerId, startDate, 7, holidays)
                graphRepository.getTickerGraph(
                    tickerId = tickerId,
                    startDate = startDate,
                    endDate = endDate,
                    interval = 1,
                )
            }

            Then("주말과 휴장일을 제외한 모든 영업일의 데이터를 반환해야 한다") {
                // 8/18(월), 8/19(화), 8/21(목), 8/22(금) -> 총 4일
                result shouldHaveSize 4
                val resultDates = result.map { it.date() }
                resultDates shouldContainExactlyInAnyOrder listOf(
                    LocalDate.of(2025, 8, 18),
                    LocalDate.of(2025, 8, 19),
                    LocalDate.of(2025, 8, 21),
                    LocalDate.of(2025, 8, 22)
                )
            }
        }
    }

    Given("7일 간격 조회 시, 간격에 해당하는 날짜가 주말이나 휴장일인 경우가 포함될때") {
        val tickerId = UUID.randomUUID()
        val startDate = LocalDate.of(2025, 7, 10)
        val endDate = LocalDate.of(2025, 7, 28)
        val holidays = setOf(LocalDate.of(2025, 7, 21))

        When("findByInterval을 호출하면") {
            val result = transaction {
                setupGraphData(tickerId, startDate.minusDays(7), 26, holidays)
                graphRepository.getTickerGraph(
                    tickerId = tickerId,
                    startDate = startDate,
                    endDate = endDate,
                    interval = 7,
                )
            }

            Then("휴장일을 가장 가까운 과거 영업일로 대체하여 데이터를 조회해야 한다") {
                result shouldHaveSize 3
                val resultDates = result.map { it.date() }
                resultDates shouldContainExactlyInAnyOrder listOf(
                    LocalDate.of(2025, 7, 28), // endDate(7/28, 월)
                    LocalDate.of(2025, 7, 18), // 7/28-7일=7/21(월, 휴일) -> 7/18(금)
                    LocalDate.of(2025, 7, 14)  // 7/21-7일=7/14(월)
                )
            }
        }

        When("endDate가 휴장일(7월 21일)일 때 findByInterval을 호출하면") {
            val result = transaction {
                setupGraphData(tickerId, startDate.minusDays(7), 26, holidays)
                graphRepository.getTickerGraph(
                    tickerId = tickerId,
                    startDate = LocalDate.of(2025, 7, 10),
                    endDate = LocalDate.of(2025, 7, 21), // 월요일, 휴장일
                    interval = 7,
                )
            }

            Then("연속된 비영업일을 건너뛰어 정확한 과거 영업일을 찾아야 한다") {
                result shouldHaveSize 2
                val resultDates = result.map { it.date() }

                // endDate(7/21,월,휴일) -> 7/18(금)
                // 7/21-7일=7/14(월) -> 7/14(월)
                resultDates shouldContainExactlyInAnyOrder listOf(
                    LocalDate.of(2025, 7, 18),
                    LocalDate.of(2025, 7, 14)
                )
            }
        }
    }

})