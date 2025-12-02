package finn.repository.exposed

import finn.TestApplication
import finn.exception.CriticalDataOmittedException
import finn.repository.ArticleSummaryRepository
import finn.table.ArticleSummaryAllTable
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@SpringBootTest(classes = [TestApplication::class, ArticleSummaryRepositoryImplTest.TestClockConfig::class])
class ArticleSummaryRepositoryImplTest(
    private val repository: ArticleSummaryRepository
) : BehaviorSpec({

    // 1. 테스트용 고정 시간 설정 (2025-12-01)
    val fixedInstant = Instant.parse("2025-12-01T00:00:00Z")
    val zoneId = ZoneId.of("UTC")
    val fixedClock = Clock.fixed(fixedInstant, zoneId)

    // 4. 각 테스트 케이스 실행 전/후 스키마 관리
    beforeContainer {
        transaction {
            SchemaUtils.create(ArticleSummaryAllTable)
        }
    }

    afterContainer {
        transaction {
            SchemaUtils.drop(ArticleSummaryAllTable)
        }
    }

    Given("종합 뉴스 데이터 조회 시") {

        When("오늘 날짜(2025-12-01)의 데이터가 존재하면") {
            transaction {
                ArticleSummaryAllTable.insert {
                    it[summaryDate] = fixedInstant
                    it[positiveReasoning] = "시장 상승 요인 분석 내용..."
                    it[negativeReasoning] = "시장 하락 리스크 분석 내용..."
                    it[positiveKeywords] = "상승, 기대, 성장"
                    it[negativeKeywords] = "금리, 인플레이션"
                    it[createdAt] = LocalDateTime.now(fixedClock)
                }
            }

            Then("데이터를 정상적으로 반환해야 한다") {
                transaction {
                    val result = repository.findSummaryAll()
                    result shouldNotBe null
                }
            }
        }

        When("어제 날짜(2025-11-30)의 데이터만 존재하면") {
            transaction {
                ArticleSummaryAllTable.insert {
                    it[summaryDate] = fixedInstant.minusSeconds(86400) // 하루 전의 데이터
                    it[positiveReasoning] = "어제 긍정 요인"
                    it[negativeReasoning] = "어제 부정 요인"
                    it[positiveKeywords] = "어제, 키워드"
                    it[negativeKeywords] = "어제, 리스크"
                    it[createdAt] = LocalDateTime.now(fixedClock).minusDays(1)
                }
            }

            Then("CriticalDataOmittedException 예외를 던져야 한다") {
                transaction {
                    val exception = shouldThrow<CriticalDataOmittedException> {
                        repository.findSummaryAll()
                    }
                    exception.message shouldBe "금일 날짜로 생성된 종합 뉴스 데이터를 찾을 수 없습니다."
                }
            }
        }

        When("데이터가 아예 존재하지 않으면") {
            // 테이블이 비어있는 상태

            Then("CriticalDataOmittedException 예외를 던져야 한다") {
                transaction {
                    shouldThrow<CriticalDataOmittedException> {
                        repository.findSummaryAll()
                    }
                }
            }
        }

        When("오늘보다 미래의 데이터(내일)가 존재하면") {
            transaction {
                ArticleSummaryAllTable.insert {
                    it[summaryDate] = fixedInstant.plusSeconds(86400) // 하루 뒤의 데이터
                    it[positiveReasoning] = "미래 긍정 요인"
                    it[negativeReasoning] = "미래 부정 요인"
                    it[positiveKeywords] = "미래, 키워드"
                    it[negativeKeywords] = "미래, 리스크"
                    it[createdAt] = LocalDateTime.now(fixedClock).plusDays(1)
                }
            }

            Then("데이터를 정상적으로 반환해야 한다 (greaterEq 조건)") {
                transaction {
                    val result = repository.findSummaryAll()
                    result shouldNotBe null
                }
            }
        }
    }
}) {
    // [핵심] 테스트 전용 설정 클래스: Clock 빈을 재정의
    @TestConfiguration
    class TestClockConfig {
        @Bean
        @Primary // 실제 애플리케이션의 Clock 빈보다 우선적으로 사용됨
        fun fixedClock(): Clock {
            return Clock.fixed(Instant.parse("2025-12-01T00:00:00Z"), ZoneId.of("UTC"))
        }
    }
}