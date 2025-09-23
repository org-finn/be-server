package finn.coroutine

import finn.TestApplication
import finn.entity.PredictionExposed
import finn.repository.PredictionRepository
import finn.table.PredictionTable
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.util.*

@SpringBootTest(classes = [TestApplication::class])
class CoroutineTxTest(
    private val predictionRepository: PredictionRepository
) : BehaviorSpec({

    val testTickerId = UUID.fromString("e219f8a8-586f-410b-a164-41a60c0c624c")
    val pDate = LocalDateTime.of(2025, 1, 1, 0, 0, 0)

    val virtualService = VirtualPredictionService(predictionRepository)

    beforeEach {
        transaction {
            PredictionTable.deleteAll() // 이전 테스트 데이터 삭제
            PredictionTable.insert {
                it[id] = EntityID(testTickerId, PredictionTable)
                it[predictionDate] = pDate
                it[score] = 50
                it[tickerId] = testTickerId
                it[positiveArticleCount] = 0L
                it[negativeArticleCount] = 0L
                it[neutralArticleCount] = 0L
                it[sentiment] = 0
                it[strategy] = "관망"
                it[tickerCode] = "AAAA"
                it[shortCompanyName] = "Company A"
                it[createdAt] = LocalDateTime.now()
            }
        }
    }

    Given("비동기 트랜잭션 내의 I/O 작업이 주어졌을 때") {
        When("해당 작업을 코루틴으로 실행하면") {
            Then("호출 스레드를 블로킹하지 않고 즉시 다음 코드를 실행한다") {
                runTest { // 코루틴 테스트 환경
                    // 5초가 걸리는 작업을 launch로 실행
                    launch {
                        newSuspendedTransaction(context = Dispatchers.IO) {
                            virtualService.updatePredictionByArticleWithDelay(
                                testTickerId, pDate, 1, 5, 13,
                                100, 1, "강한 매수", delayMillis = 5000L
                            )
                        }
                    }

                    // launch 호출 직후의 가상 시간은 거의 0이어야 함
                    currentTime shouldBeLessThan 100L

                    // 가상 시간을 진행시켜 백그라운드 작업 완료
                    advanceTimeBy(5000)
                }
            }
        }
    }

    Given("비동기 트랜잭션 내에서 예외가 발생하는 상황이 주어졌을 때") {
        When("트랜잭션 작업을 실행하면") {
            Then("예외가 발생하고 모든 DB 변경사항은 롤백되어야 한다") {
                runTest {
                    // 예외를 발생시키는 메서드 호출
                    shouldThrow<RuntimeException> {
                        newSuspendedTransaction(context = Dispatchers.IO) {
                            virtualService.updatePredictionByArticleAndThrow(
                                testTickerId, pDate, 1, 0, 0,
                                100, 1, "강한 매수"
                            )
                        }
                    }

                    // 예외 발생 후 DB 상태를 다시 조회
                    val predictionAfterException = transaction {
                        PredictionExposed.find { PredictionTable.tickerId eq testTickerId }.single()
                    }

                    // 모든 값은 초기값을 유지해야 함 (롤백 검증)
                    predictionAfterException.score shouldBe 50
                    predictionAfterException.positiveArticleCount shouldBe 0L
                    predictionAfterException.neutralArticleCount shouldBe 0L
                    predictionAfterException.negativeArticleCount shouldBe 0L
                    predictionAfterException.sentiment shouldBe 0
                    predictionAfterException.strategy shouldBe "관망"
                }
            }
        }
    }
})


