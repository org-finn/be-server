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

    override fun save(
        userId: UUID, deviceId: UUID, deviceType: String, tokenValue: String
    ) {
        userTokenExposedRepository.save(
            userId,
            deviceId,
            deviceType,
            tokenValue
        )
    }

    override fun findByDeviceId(deviceId: UUID): UserToken {
        return toDomain(userTokenExposedRepository.findByDeviceId(deviceId))
    }

    override fun updateRefreshToken(
        deletingRefreshToken: String,
        refreshToken: String,
        userId: UUID,
        deviceId: UUID,
        deviceType: String
    ) {
        userTokenExposedRepository.update(
            deletingRefreshToken,
            refreshToken,
            userId,
            deviceId,
            deviceType
        )
    }

    override fun releaseRefreshToken(deviceId: UUID) {
        userTokenExposedRepository.release(deviceId)
    }

    override fun deleteRefreshToken(deviceId: UUID) {
        userTokenExposedRepository.deleteByDeviceId(deviceId)
    }
}