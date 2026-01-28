package finn.orchestrator

import finn.response.userinfo.NicknameValidationResponse
import finn.service.UserInfoService
import finn.transaction.ExposedTransactional
import org.springframework.stereotype.Service

@Service
@ExposedTransactional(readOnly = true)
class UserInfoOrchestrator(
    private val userInfoService: UserInfoService
) {

    fun checkNicknameValidation(nickname: String): NicknameValidationResponse {
        return NicknameValidationResponse(userInfoService.checkNicknameValidation(nickname))
    }

}