package finn.controller

import finn.apiSpec.UserInfoApiSpec
import finn.orchestrator.UserInfoOrchestrator
import finn.request.userinfo.FavoriteTickerRequest
import finn.request.userinfo.NicknameRequest
import finn.response.SuccessResponse
import finn.response.userinfo.FavoriteTickerResponse
import finn.response.userinfo.NicknameValidationResponse
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class UserInfoController(
    private val userInfoOrchestrator: UserInfoOrchestrator
) : UserInfoApiSpec {

    override fun checkNicknameValidation(nickname: String): SuccessResponse<NicknameValidationResponse> {
        val response = userInfoOrchestrator.checkNicknameValidation(nickname)
        return SuccessResponse("200 Ok", "닉네임 중복 검사 조회 성공", response)
    }

    override fun updateNickname(nicknameRequest: NicknameRequest): SuccessResponse<Nothing> {
        val userId = UUID.randomUUID() // [TODO]: userId 추출
        userInfoOrchestrator.updateNickname(nicknameRequest.nickname, userId)
        return SuccessResponse("204 No Content", "닉네임 수정 성공", null)
    }

    override fun getFavoriteTickers(): SuccessResponse<FavoriteTickerResponse> {
        val userId = UUID.randomUUID() // [TODO] : userId 추출 로직 구현 필요
        val response = userInfoOrchestrator.getFavoriteTickers(userId)
        return SuccessResponse("200 Ok", "관심 종목 리스트 조회 성공", response)
    }

    override fun updateFavoriteTickers(favoriteTickerRequest: FavoriteTickerRequest): SuccessResponse<Nothing> {
        val userId = UUID.randomUUID() // [TODO] : userId 추출 로직 구현 필요
        userInfoOrchestrator.updateFavoriteTickers(userId, favoriteTickerRequest)
        return SuccessResponse("204 No Content", "관심 종목 리스트 수정 성공", null)
    }

    override fun updateFavoriteSingleTicker(
        tickerCode: String,
        mode: String
    ): SuccessResponse<Nothing> {
        val userId = UUID.randomUUID() // [TODO] : userId 추출 로직 구현 필요
        userInfoOrchestrator.updateFavoriteTickerSingle(userId, tickerCode, mode)
        return SuccessResponse("204 No Content", "관심 종목 등록/해제 성공", null)
    }

    override fun withdrawn(): SuccessResponse<Nothing> {
        val userId = UUID.randomUUID() // [TODO] : userId 추출 로직 구현 필요
        userInfoOrchestrator.withdrawn(userId)
        return SuccessResponse("204 No Content", "회원 탈퇴 성공", null)
    }
}