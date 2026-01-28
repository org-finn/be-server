package finn.repository.impl

import finn.entity.UserInfo
import finn.mapper.toDomain
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
}