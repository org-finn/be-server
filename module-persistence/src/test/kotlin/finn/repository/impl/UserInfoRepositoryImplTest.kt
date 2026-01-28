package finn.repository.impl

import finn.TestApplication
import finn.entity.UserInfoExposed
import finn.exception.DomainPolicyViolationException
import finn.exception.NotFoundDataException
import finn.repository.UserInfoRepository
import finn.table.UserInfoTable
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
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
                        it[role] = "USER"
                        it[status] = "REGISTERED"
                        it[createdAt] = LocalDateTime.now()
                        it[updatedAt] = LocalDateTime.now()
                    }.value

                    UserInfoTable.insertAndGetId {
                        it[nickname] = "user_b" // 이미 존재하는 닉네임
                        it[oauthUserId] = UUID.randomUUID()
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
})
