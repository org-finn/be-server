package finn.repository.impl

import finn.entity.UserToken
import finn.mapper.toDomain
import finn.repository.UserTokenRepository
import finn.repository.exposed.UserTokenExposedRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class UserTokenRepositoryImpl(
    private val userTokenExposedRepository: UserTokenExposedRepository
) : UserTokenRepository {

    override fun save(userId: UUID, deviceId: UUID, deviceType: String, tokenValue: String,
                      expiredAt: Date, issuedAt: Date) {
        userTokenExposedRepository.save(
            userId,
            deviceId,
            deviceType,
            tokenValue,
            expiredAt,
            issuedAt)
    }

    override fun findByDeviceId(deviceId: UUID): UserToken {
        return toDomain(userTokenExposedRepository.findByDeviceId(deviceId))
    }

    override fun updateRefreshToken(refreshToken: String, deviceId: UUID): Boolean {
        return userTokenExposedRepository.update(refreshToken, deviceId)
    }

    override fun deleteRefreshToken(deviceId: UUID) {
        userTokenExposedRepository.delete(deviceId)
    }
}