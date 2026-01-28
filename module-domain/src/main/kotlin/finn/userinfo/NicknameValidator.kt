package finn.userinfo

import finn.exception.DomainPolicyViolationException
import org.springframework.stereotype.Component

@Component
class NicknameValidator {

    private val NICKNAME_REGEX = "^[가-힣a-zA-Z0-9]{1,12}$".toRegex()

    fun isValid(nickname: String?): Boolean {
        if (nickname.isNullOrBlank()) {
            throw DomainPolicyViolationException("유효하지 않은 조건의 닉네임입니다. (영대소문자/한글/숫자 조합으로 최대 12자)")
        }
        return NICKNAME_REGEX.matches(nickname)
    }
}