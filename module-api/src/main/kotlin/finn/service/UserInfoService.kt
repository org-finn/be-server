package finn.service

import finn.entity.UserInfo
import finn.entity.UserRole
import finn.entity.UserStatus
import finn.queryDto.FavoriteTickerQueryDto
import finn.repository.TickerRepository
import finn.repository.UserInfoRepository
import finn.userinfo.NicknameProvider
import finn.userinfo.NicknameValidator
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserInfoService(
    private val userInfoRepository: UserInfoRepository,
    private val tickerRepository: TickerRepository,
    private val nicknameProvider: NicknameProvider,
    private val nicknameValidator: NicknameValidator
) {

    fun createUserInfo(oAuthUserId: UUID): UserInfo {
        val nickname = nicknameProvider.createUniqueNickname()
        return userInfoRepository.save(
            oAuthUserId,
            nickname,
            UserRole.USER.name,
            UserStatus.REGISTERED.name
        )
    }

    fun checkNicknameValidation(nickname: String): Boolean {
        nicknameValidator.isValid(nickname)
        return userInfoRepository.existNickname(nickname)
    }

    fun updateNickname(nickname: String, userId: UUID) {
        nicknameValidator.isValid(nickname)
        userInfoRepository.updateNickname(nickname, userId)
    }

    fun getFavoriteTickers(userId: UUID): List<FavoriteTickerQueryDto> {
        return userInfoRepository.findFavoriteTickers(userId)
    }

    fun updateFavoriteTickers(userId: UUID, newTickers: List<String>) {
        // 1. 유효한 종목들인지 선 검증
        tickerRepository.validTickersByTickerCode(newTickers)
        // 2. 기존 favorite_tickers 뒤집어쓰기
        userInfoRepository.updateFavoriteTickers(userId, newTickers)
    }

    fun updateFavoriteTickerSingle(userId: UUID, tickerCode: String, mode: String) {
        userInfoRepository.updateFavoriteTicker(userId, tickerCode, mode)
    }

    fun withdrawn(userId: UUID) {
        userInfoRepository.deleteUserInfo(userId, UserStatus.WITHDRAWN.name)
    }

    fun getUserInfo(userId: UUID): UserInfo {
        return userInfoRepository.findById(userId)
    }
}