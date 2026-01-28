package finn.orchestrator

import finn.response.userinfo.NicknameValidationResponse
import finn.service.UserInfoService
import finn.transaction.ExposedTransactional
import org.springframework.stereotype.Service
import java.util.*

@Service
@ExposedTransactional(readOnly = true)
class UserInfoOrchestrator(
    private val userInfoService: UserInfoService
) {

    fun checkNicknameValidation(nickname: String): NicknameValidationResponse {
        return NicknameValidationResponse(userInfoService.checkNicknameValidation(nickname))
    }

    fun updateNickname(nickname: String, userId: UUID) {
        userInfoService.updateNickname(nickname, userId)
    }
}