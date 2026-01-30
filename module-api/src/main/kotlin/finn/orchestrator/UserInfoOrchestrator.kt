package finn.orchestrator

import finn.mapper.FavoriteTIckerDtoMapper.Companion.toDto
import finn.mapper.UserInfoDtoMapper
import finn.request.userinfo.FavoriteTickerRequest
import finn.response.userinfo.FavoriteTickerResponse
import finn.response.userinfo.NicknameValidationResponse
import finn.response.userinfo.UserInfoResponse
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

    fun getFavoriteTickers(userId: UUID): FavoriteTickerResponse {
        return toDto(userInfoService.getFavoriteTickers(userId))
    }

    fun updateFavoriteTickers(userId: UUID, newTickerRequest: FavoriteTickerRequest) {
        userInfoService.updateFavoriteTickers(userId, newTickerRequest.tickers)
    }

    fun updateFavoriteTickerSingle(userId: UUID, tickerCode: String, mode: String) {
        userInfoService.updateFavoriteTickerSingle(userId, tickerCode, mode)
    }

    fun withdrawn(userId: UUID) {
        userInfoService.withdrawn(userId)
    }

    fun getUserInfo(userId: UUID): UserInfoResponse {
        return UserInfoDtoMapper.toDto(userInfoService.getUserInfo(userId))
    }
}