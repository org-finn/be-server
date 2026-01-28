package finn.repository

import finn.entity.UserToken
import java.util.*

interface UserTokenRepository {

    fun save(
        userId: UUID, deviceId: UUID, deviceType: String, tokenValue: String
    )

    fun findByDeviceId(deviceId: UUID): UserToken

    fun updateRefreshToken(
        deletingRefreshToken: String,
        refreshToken: String,
        userId: UUID,
        deviceId: UUID,
        deviceType: String
    )

    fun releaseRefreshToken(deviceId: UUID)

    fun deleteRefreshToken(deviceId: UUID)
}