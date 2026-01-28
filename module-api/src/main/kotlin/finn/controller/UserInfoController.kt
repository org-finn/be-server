package finn.controller

import finn.apiSpec.UserInfoApiSpec
import finn.orchestrator.UserInfoOrchestrator
import finn.request.userinfo.NicknameRequest
import finn.response.SuccessResponse
import finn.response.userinfo.NicknameValidationResponse
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class UserInfoController(
    private val userInfoOrchestrator: UserInfoOrchestrator
) : UserInfoApiSpec {

    override fun checkNicknameValidation(nickname: String): SuccessResponse<NicknameValidationResponse> {
        val response = userInfoOrchestrator.checkNicknameValidation(nickname)
        return SuccessResponse("200 Ok","닉네임 중복 검사 조회 성공", response)
    }

    override fun updateNickname(nicknameRequest: NicknameRequest): SuccessResponse<Nothing> {
        val userId = UUID.randomUUID() // [TODO]: userId 추출
        userInfoOrchestrator.updateNickname(nicknameRequest.nickname, userId)
        return SuccessResponse("204 No Content", "닉네임 수정 성공", null)
    }
}