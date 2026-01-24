package finn.test.integration

import finn.TestApplication
import finn.queryDto.PredictionUpdateDto
import finn.repository.exposed.PredictionExposedRepository
import finn.table.PredictionTable
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.longs.shouldBeExactly
import io.kotest.matchers.shouldNotBe
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger


@SpringBootTest(classes = [TestApplication::class])
class PredictionConcurrencyTest(
    private val predictionRepository: PredictionExposedRepository
) : BehaviorSpec() {

    // Spring TestContext를 사용하기 위한 확장 등록
    override fun extensions(): List<Extension> = listOf(SpringExtension)
    private val ALPHA = 0.1

    init {
        val targetTickerId = UUID.randomUUID()
        val targetDate = LocalDate.now()

        // 테스트 전후 데이터 초기화
        beforeSpec {
            transaction {
                PredictionTable.deleteAll()
                // 초기 데이터 세팅 (점수 0, 카운트 0)
                PredictionTable.insert {
                    it[tickerId] = targetTickerId
                    it[tickerCode] = ""
                    it[shortCompanyName] = ""
                    it[predictionDate] = targetDate.atStartOfDay() // 00:00:00 고정
                    it[volatility] = BigDecimal.ZERO
                    it[score] = 50
                    it[positiveArticleCount] = 0
                    it[negativeArticleCount] = 0
                    it[neutralArticleCount] = 0
                    it[sentiment] = 0
                    it[strategy] = "관망"
                    it[createdAt] = LocalDateTime.now()
                }
            }
        }

        afterSpec {
            transaction {
                PredictionTable.deleteAll()
            }
        }

        Given("동시성 테스트 환경에서") {
            val numberOfThreads = 10      // 동시 실행 스레드 수
            val batchesPerThread = 10     // 스레드당 배치 반복 횟수
            val updatesPerBatch = 10      // 배치당 업데이트 건수

            // 총 기대 증가량 = 10 * 10 * 10 = 1000
            val expectedTotalCount = (numberOfThreads * batchesPerThread * updatesPerBatch).toLong()

            val startLatch = CountDownLatch(1)
            val doneLatch = CountDownLatch(numberOfThreads)
            val executor = Executors.newFixedThreadPool(numberOfThreads)
            val errorCount = AtomicInteger(0)

            When("10개의 스레드가 동시에 같은 종목에 대해 배치를 수행하면") {
                repeat(numberOfThreads) {
                    executor.submit {
                        try {
                            // 모든 스레드 대기 (동시 출발을 위해)
                            startLatch.await()

                            repeat(batchesPerThread) {
                                val batchUpdates = List(updatesPerBatch) {
                                    PredictionUpdateDto(
                                        tickerId = targetTickerId,
                                        predictionDate = targetDate.atStartOfDay(),
                                        score = 60,
                                        positiveArticleCount = 1, // +1
                                        negativeArticleCount = 1, // +1
                                        neutralArticleCount = 1,  // +1
                                        sentiment = 1,
                                        strategy = "약한 호재"
                                    )
                                }

                                // [중요] 스레드별로 별도의 트랜잭션을 열어서 실행해야 합니다.
                                // Repository 내부에서 TransactionManager.current()를 쓰기 때문입니다.
                                transaction {
                                    predictionRepository.batchUpdatePredictions(batchUpdates, ALPHA)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            errorCount.incrementAndGet()
                        } finally {
                            doneLatch.countDown()
                        }
                    }
                }

                // 땅! 동시 시작
                startLatch.countDown()
                // 모두 끝날 때까지 대기
                doneLatch.await()

                Then("갱신 분실(Lost Update) 없이 카운트가 정확히 1,000 증가해야 한다") {
                    // 에러가 없었는지 먼저 확인
                    errorCount.toInt() shouldBeExactly 0

                    // DB 결과 검증
                    transaction {
                        val result = PredictionTable.select(
                            PredictionTable.positiveArticleCount,
                            PredictionTable.score
                        )
                            .where { (PredictionTable.tickerId eq targetTickerId) and (PredictionTable.predictionDate eq targetDate.atStartOfDay()) }
                            .single()

                        val finalPositiveCount = result[PredictionTable.positiveArticleCount]
                        val finalScore = result[PredictionTable.score]

                        println("=========================================")
                        println("Expected Count : $expectedTotalCount")
                        println("Actual Count   : $finalPositiveCount")
                        println("Final Score    : $finalScore")
                        println("=========================================")

                        // 1. 카운트 정합성 검증
                        finalPositiveCount shouldBeExactly expectedTotalCount

                        // 2. 점수 갱신 여부 검증 (0이 아니어야 함)
                        finalScore shouldNotBe 0
                    }
                }
            }

            // 리소스 정리
            executor.shutdown()
        }
    }
}