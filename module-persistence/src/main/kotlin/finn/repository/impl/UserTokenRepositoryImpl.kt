package finn.repository.impl

import finn.repository.UserTokenRepository
import finn.repository.exposed.UserTokenExposedRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class UserTokenRepositoryImpl(
    private val userTokenExposedRepository: UserTokenExposedRepository
)
    : UserTokenRepository{
    override fun findByDeviceId(deviceId: UUID): String {
        return userTokenExposedRepository.findByDeviceId(deviceId)
    }

    override fun updateRefreshToken(refreshToken: String, deviceId: UUID) {
        userTokenExposedRepository.update(refreshToken, deviceId)
    }

    override fun deleteRefreshToken(deviceId: UUID) {
        userTokenExposedRepository.delete(deviceId)
    }
}