package finn.repository.impl

import finn.TestApplication
import finn.entity.UserInfoExposed
import finn.exception.DomainPolicyViolationException
import finn.exception.NotFoundDataException
import finn.repository.UserInfoRepository
import finn.table.TickerTable
import finn.table.UserInfoTable
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.util.*

@SpringBootTest(classes = [TestApplication::class])
internal class UserInfoRepositoryImplTest(
    private val repository: UserInfoRepository
) : DescribeSpec({


    describe("UserInfoRepository (Integration with PostgreSQL)") {

        // ==========================================
        // 1. 닉네임 중복 검사 (nonExistNickname)
        // ==========================================
        context("nonExistNickname 메서드는") {
            it("DB에 해당 닉네임이 없으면 true를 반환한다") {
                transaction {
                    // given: 데이터 없음

                    // when
                    val result = repository.existNickname("clean_nick")

                    // then
                    result shouldBe true
                }
            }

            it("DB에 해당 닉네임이 이미 존재하면 false를 반환한다") {
                transaction {
                    // given: 데이터 미리 삽입
                    UserInfoTable.insertAndGetId {
                        it[oauthUserId] = UUID.randomUUID()
                        it[nickname] = "duplicate_nick"
                        it[role] = "USER"
                        it[status] = "REGISTERED"
                        it[createdAt] = LocalDateTime.now()
                        it[updatedAt] = LocalDateTime.now()
                    }

                    // when
                    val result = repository.existNickname("duplicate_nick")

                    // then
                    result shouldBe false
                }
            }
        }

        // ==========================================
        // 2. 닉네임 업데이트 (updateNickname)
        // ==========================================
        context("updateNickname 메서드는") {

            it("중복되지 않은 닉네임과 유효한 ID가 주어지면 업데이트에 성공한다") {
                transaction {
                    // given
                    val userId = UserInfoTable.insertAndGetId {
                        it[nickname] = "old_nick"
                        it[oauthUserId] = UUID.randomUUID()
                        it[imageUrl] = "https://test.com/test.png"
                        it[role] = "USER"
                        it[status] = "REGISTERED"
                        it[createdAt] = LocalDateTime.now()
                        it[updatedAt] = LocalDateTime.now()
                    }.value

                    // when
                    repository.updateNickname("new_nick", userId)

                    // then
                    // DB에서 다시 조회하여 변경 확인
                    val updatedUser = UserInfoExposed.findById(userId)
                    updatedUser?.nickname shouldBe "new_nick"
                }
            }

            it("이미 존재하는 닉네임으로 변경하려 하면 DomainPolicyViolationException을 던진다") {
                transaction {
                    // given: A유저와 B유저 생성
                    val userIdA = UserInfoTable.insertAndGetId {
                        it[nickname] = "user_a"
                        it[oauthUserId] = UUID.randomUUID()
                        it[imageUrl] = "https://test.com/test.png"
                        it[role] = "USER"
                        it[status] = "REGISTERED"
                        it[createdAt] = LocalDateTime.now()
                        it[updatedAt] = LocalDateTime.now()
                    }.value

                    UserInfoTable.insertAndGetId {
                        it[nickname] = "user_b" // 이미 존재하는 닉네임
                        it[oauthUserId] = UUID.randomUUID()
                        it[imageUrl] = "https://test.com/test.png"
                        it[role] = "USER"
                        it[status] = "REGISTERED"
                        it[createdAt] = LocalDateTime.now()
                        it[updatedAt] = LocalDateTime.now()
                    }

                    // when & then: A의 닉네임을 B의 닉네임("user_b")으로 변경 시도
                    shouldThrow<DomainPolicyViolationException> {
                        repository.updateNickname("user_b", userIdA)
                    }.message shouldBe "이미 존재하는 닉네임입니다."
                }
            }

            it("존재하지 않는 사용자 ID로 업데이트 시도 시 NotFoundDataException을 던진다") {
                transaction {
                    // given
                    val unknownUserId = UUID.randomUUID()

                    // when & then
                    shouldThrow<NotFoundDataException> {
                        repository.updateNickname("valid_nick", unknownUserId)
                    }.message shouldBe "존재하지 않는 사용자입니다."
                }
            }
        }
    }

    describe("UserInfoRepository (Favorite Tickers Logic)") {

        // ==========================================
        // 1. findFavoriteTickers (조회)
        // ==========================================
        context("findFavoriteTickers 메서드는") {
            it("저장된 티커 코드가 TickerTable에 존재하면 DTO 리스트를 반환한다") {
                transaction {
                    // given: 티커 정보 미리 저장
                    val aaplId = TickerTable.insertAndGetId {
                        // [필수] Unique Key
                        it[code] = "AAPL"

                        // [필수] 회사 상세 정보
                        it[fullCompanyName] = "Apple Inc."
                        it[shortCompanyName] = "Apple"
                        it[shortCompanyNameKr] = "애플"
                        it[country] = "USA"
                        it[exchangeCode] = "NASDAQ"
                        it[createdAt] = LocalDateTime.now()
                    }.value

                    val tslaId = TickerTable.insertAndGetId {
                        it[code] = "TSLA"
                        it[fullCompanyName] = "Tesla Inc."
                        it[shortCompanyName] = "Tesla"
                        it[shortCompanyNameKr] = "테슬라"
                        it[country] = "USA"
                        it[exchangeCode] = "NASDAQ"
                        it[createdAt] = LocalDateTime.now()
                    }.value

                    TickerTable.insertAndGetId {
                        it[code] = "MSFT"
                        it[fullCompanyName] = "Microsoft Inc."
                        it[shortCompanyName] = "Microsoft"
                        it[shortCompanyNameKr] = "마이크로소프트"
                        it[country] = "USA"
                        it[exchangeCode] = "NASDAQ"
                        it[createdAt] = LocalDateTime.now()
                    } // 유저가 관심없는 티커

                    // given: 유저 생성 (AAPL, TSLA 등록)
                    val userId = UserInfoTable.insertAndGetId {
                        it[nickname] = "tester"
                        it[oauthUserId] = UUID.randomUUID()
                        it[imageUrl] = "https://test.com/test.png"
                        it[role] = "MEMBER"
                        it[status] = "ACTIVE"
                        it[favoriteTickers] = "AAPL,TSLA"
                        it[createdAt] = LocalDateTime.now()
                        it[updatedAt] = LocalDateTime.now()
                    }.value

                    // when
                    val result = repository.findFavoriteTickers(userId)

                    // then
                    result.size shouldBe 2
                    result.map { it.tickerCode } shouldContainExactlyInAnyOrder listOf("AAPL", "TSLA")
                    result.map { it.tickerId } shouldContainExactlyInAnyOrder listOf(aaplId, tslaId)
                }
            }

            it("저장된 관심 티커가 없으면 빈 리스트를 반환한다") {
                transaction {
                    // given
                    val userId = UserInfoTable.insertAndGetId {
                        it[nickname] = "tester"
                        it[oauthUserId] = UUID.randomUUID()
                        it[imageUrl] = "https://test.com/test.png"
                        it[role] = "USER"
                        it[status] = "REGISTERED"
                        it[favoriteTickers] = "" // or null logic check
                        it[createdAt] = LocalDateTime.now()
                        it[updatedAt] = LocalDateTime.now()
                    }.value

                    // when
                    val result = repository.findFavoriteTickers(userId)

                    // then
                    result shouldBe emptyList()
                }
            }
        }

        // ==========================================
        // 2. updateFavoriteTicker (단일 추가/삭제 - Mode)
        // ==========================================
        context("updateFavoriteTicker 메서드는") {

            it("mode가 'on'이면 티커를 추가한다 (기존 목록 유지)") {
                transaction {
                    // given: 이미 AAPL이 있는 유저
                    val userId = UserInfoTable.insertAndGetId {
                        it[nickname] = "tester"
                        it[oauthUserId] = UUID.randomUUID()
                        it[imageUrl] = "https://test.com/test.png"
                        it[role] = "USER"
                        it[status] = "REGISTERED"
                        it[favoriteTickers] = "AAPL"
                        it[createdAt] = LocalDateTime.now()
                        it[updatedAt] = LocalDateTime.now()
                    }.value

                    // when: TSLA 추가
                    repository.updateFavoriteTicker(userId, "TSLA", "on")

                    // then
                    val user = UserInfoExposed.findById(userId)!!
                    // Set 자료구조 특성상 순서 보장은 안될 수 있으므로 포함 여부 확인
                    user.favoriteTickers!!.split(",") shouldContainExactlyInAnyOrder listOf("AAPL", "TSLA")
                }
            }

            it("mode가 'on'일 때 이미 존재하는 티커면 중복해서 추가하지 않는다 (Set 동작)") {
                transaction {
                    // given: AAPL 보유
                    val userId = UserInfoTable.insertAndGetId {
                        it[nickname] = "tester"
                        it[oauthUserId] = UUID.randomUUID()
                        it[imageUrl] = "https://test.com/test.png"
                        it[role] = "USER"
                        it[status] = "REGISTERED"
                        it[favoriteTickers] = "AAPL"
                        it[createdAt] = LocalDateTime.now()
                        it[updatedAt] = LocalDateTime.now()
                    }.value

                    // when: AAPL 다시 추가
                    repository.updateFavoriteTicker(userId, "AAPL", "on")

                    // then: 여전히 AAPL 하나여야 함
                    val user = UserInfoExposed.findById(userId)!!
                    user.favoriteTickers shouldBe "AAPL"
                }
            }

            it("mode가 'off'이면 티커를 삭제한다") {
                transaction {
                    // given: AAPL, TSLA 보유
                    val userId = UserInfoTable.insertAndGetId {
                        it[nickname] = "tester"
                        it[oauthUserId] = UUID.randomUUID()
                        it[imageUrl] = "https://test.com/test.png"
                        it[role] = "USER"
                        it[status] = "REGISTERED"
                        it[favoriteTickers] = "AAPL,TSLA"
                        it[createdAt] = LocalDateTime.now()
                        it[updatedAt] = LocalDateTime.now()
                    }.value

                    // when: AAPL 삭제
                    repository.updateFavoriteTicker(userId, "AAPL", "off")

                    // then: TSLA만 남아야 함
                    val user = UserInfoExposed.findById(userId)!!
                    user.favoriteTickers shouldBe "TSLA"
                }
            }

            it("유효하지 않은 mode 값이면 DomainPolicyViolationException을 던진다") {
                transaction {
                    val userId = UserInfoTable.insertAndGetId {
                        it[nickname] = "tester"
                        it[oauthUserId] = UUID.randomUUID()
                        it[imageUrl] = "https://test.com/test.png"
                        it[role] = "USER"
                        it[status] = "REGISTERED"
                        it[createdAt] = LocalDateTime.now()
                        it[updatedAt] = LocalDateTime.now()
                    }.value

                    shouldThrow<DomainPolicyViolationException> {
                        repository.updateFavoriteTicker(userId, "AAPL", "unknown_mode")
                    }.message shouldBe "유효하지 않은 변경 상태 모드 값입니다."
                }
            }
        }

        // ==========================================
        // 3. updateFavoriteTickers (전체 수정)
        // ==========================================
        context("updateFavoriteTickers 메서드는 (List)") {
            it("기존 목록을 무시하고 새로운 리스트로 덮어쓴다") {
                transaction {
                    // given: 기존 AAPL
                    val userId = UserInfoTable.insertAndGetId {
                        it[nickname] = "tester"
                        it[oauthUserId] = UUID.randomUUID()
                        it[imageUrl] = "https://test.com/test.png"
                        it[role] = "USER"
                        it[status] = "REGISTERED"
                        it[favoriteTickers] = "AAPL"
                        it[createdAt] = LocalDateTime.now()
                        it[updatedAt] = LocalDateTime.now()
                    }.value

                    // when: [TSLA, MSFT]로 교체
                    val newTickers = listOf("TSLA", "MSFT")
                    repository.updateFavoriteTickers(userId, newTickers)

                    // then
                    val user = UserInfoExposed.findById(userId)!!
                    user.favoriteTickers!!.split(",") shouldContainExactlyInAnyOrder listOf("TSLA", "MSFT")
                }
            }
        }
    }
})
