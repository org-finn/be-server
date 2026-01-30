package finn.controller

import finn.apiSpec.UserInfoApiSpec
import finn.orchestrator.UserInfoOrchestrator
import finn.request.userinfo.FavoriteTickerRequest
import finn.request.userinfo.NicknameRequest
import finn.response.SuccessResponse
import finn.response.userinfo.FavoriteTickerResponse
import finn.response.userinfo.NicknameValidationResponse
import finn.response.userinfo.UserInfoResponse
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

    override fun updateNickname(
        nicknameRequest: NicknameRequest,
        userId: UUID
    ): SuccessResponse<Nothing> {
        userInfoOrchestrator.updateNickname(nicknameRequest.nickname, userId)
        return SuccessResponse("204 No Content", "닉네임 수정 성공", null)
    }

    override fun getFavoriteTickers(userId: UUID): SuccessResponse<FavoriteTickerResponse> {
        val response = userInfoOrchestrator.getFavoriteTickers(userId)
        return SuccessResponse("200 Ok", "관심 종목 리스트 조회 성공", response)
    }

    override fun updateFavoriteTickers(
        favoriteTickerRequest: FavoriteTickerRequest,
        userId: UUID
    ): SuccessResponse<Nothing> {
        userInfoOrchestrator.updateFavoriteTickers(userId, favoriteTickerRequest)
        return SuccessResponse("204 No Content", "관심 종목 리스트 수정 성공", null)
    }

    override fun updateFavoriteSingleTicker(
        tickerCode: String,
        mode: String,
        userId: UUID
    ): SuccessResponse<Nothing> {
        userInfoOrchestrator.updateFavoriteTickerSingle(userId, tickerCode, mode)
        return SuccessResponse("204 No Content", "관심 종목 등록/해제 성공", null)
    }

    override fun withdrawn(userId: UUID): SuccessResponse<Nothing> {
        userInfoOrchestrator.withdrawn(userId)
        return SuccessResponse("204 No Content", "회원 탈퇴 성공", null)
    }

    override fun getUserInfo(userId: UUID): SuccessResponse<UserInfoResponse> {
        val response = userInfoOrchestrator.getUserInfo(userId)
        return SuccessResponse("200 Ok", "유저 정보 조회 성공", response)
    }
}