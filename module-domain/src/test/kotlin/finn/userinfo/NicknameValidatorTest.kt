package finn.userinfo

import finn.exception.DomainPolicyViolationException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class NicknameValidatorTest : DescribeSpec({

    val validator = NicknameValidator()

    describe("NicknameValidator") {

        context("유효한 닉네임이 주어지면") {
            it("true를 반환한다") {
                // 한글, 영문(대소문자), 숫자, 혼합, 길이 경계값(1자, 12자) 테스트
                val validNicknames = listOf(
                    "Finn",          // 영문
                    "홍길동",         // 한글
                    "user123",       // 영문+숫자
                    "개발자",         // 한글
                    "A",             // 최소 길이 (1자)
                    "가나다라마바사아자차카타" // 최대 길이 (12자)
                )

                validNicknames.forEach { nickname ->
                    validator.isValid(nickname) shouldBe true
                }
            }
        }

        context("형식에 맞지 않는 닉네임이 주어지면 (특수문자, 공백, 길이 초과 등)") {
            it("false를 반환한다") {
                val invalidNicknames = listOf(
                    "Finn!",         // 특수문자 포함
                    "user name",     // 공백 포함
                    "test@email",    // 특수문자(@) 포함
                    "가나다라마바사아자차카타파", // 길이 초과 (13자)
                    "ㅋㅋㅋ",          // 자음/모음 단독 (Regex가 '가-힣'이므로 완성형만 허용됨)
                    "ㅏㅏㅏ"           // 모음 단독
                )

                invalidNicknames.forEach { nickname ->
                    validator.isValid(nickname) shouldBe false
                }
            }
        }

        context("null 또는 빈 문자열(Blank)이 주어지면") {
            it("DomainPolicyViolationException을 던진다") {
                val blankNicknames = listOf(null, "", "   ")

                blankNicknames.forEach { nickname ->
                    shouldThrow<DomainPolicyViolationException> {
                        validator.isValid(nickname)
                    }.message shouldBe "유효하지 않은 조건의 닉네임입니다. (영대소문자/한글/숫자 조합으로 최대 12자)"
                }
            }
        }
    }
})