package finn.orchestrator

import finn.mapper.FavoriteTIckerDtoMapper.Companion.toDto
import finn.request.userinfo.FavoriteTickerRequest
import finn.response.userinfo.FavoriteTickerResponse
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

    fun getFavoriteTickers(userId: UUID) : FavoriteTickerResponse {
        return toDto(userInfoService.getFavoriteTickers(userId))
    }

    fun updateFavoriteTickers(userId: UUID, newTickerRequest: FavoriteTickerRequest) {
        userInfoService.updateFavoriteTickers(userId, newTickerRequest.tickers)
    }
}