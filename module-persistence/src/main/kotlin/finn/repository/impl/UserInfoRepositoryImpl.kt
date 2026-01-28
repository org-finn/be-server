package finn.repository.impl

import finn.entity.UserInfo
import finn.exception.DomainPolicyViolationException
import finn.mapper.toDomain
import finn.queryDto.FavoriteTickerQueryDto
import finn.repository.UserInfoRepository
import finn.repository.exposed.UserInfoExposedRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class UserInfoRepositoryImpl(
    private val userInfoExposedRepository: UserInfoExposedRepository
) : UserInfoRepository {
    override fun findById(userInfoId: UUID): UserInfo {
        return toDomain(userInfoExposedRepository.findById(userInfoId))
    }

    override fun save(
        oAuthUserId: UUID,
        nickname: String,
        role: String,
        status: String
    ): UserInfo {
        return toDomain(userInfoExposedRepository.save(oAuthUserId, nickname, role, status))
    }

    override fun existNickname(nickname: String): Boolean {
        return userInfoExposedRepository.nonExistNickname(nickname)
    }

    override fun updateNickname(nickname: String, userId: UUID) {
        userInfoExposedRepository.updateNickname(nickname, userId)
    }

    override fun findFavoriteTickers(userId: UUID): List<FavoriteTickerQueryDto> {
        return userInfoExposedRepository.findFavoriteTickersByUserId(userId)
    }

    override fun updateFavoriteTickers(
        userId: UUID,
        tickerCodes: List<String>
    ) {
        userInfoExposedRepository.updateFavoriteTickers(userId, tickerCodes)
    }

    override fun updateFavoriteTicker(
        userId: UUID,
        tickerCode: String,
        mode: String
    ) {
        when (mode) {
            "on" -> userInfoExposedRepository.addFavoriteTicker(userId, tickerCode)
            "off" -> userInfoExposedRepository.removeFavoriteTicker(userId, tickerCode)
            else -> throw DomainPolicyViolationException("유효하지 않은 변경 상태 모드 값입니다.")
        }
    }

    override fun deleteUserInfo(userId: UUID, status: String) {
        userInfoExposedRepository.delete(userId, status)
    }
}