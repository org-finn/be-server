package finn.repository

import java.util.*

interface UserTokenRepository {

    fun findByDeviceId(deviceId: UUID): String

    fun updateRefreshToken(refreshToken: String, deviceId: UUID)

    fun deleteRefreshToken(deviceId: UUID)
}