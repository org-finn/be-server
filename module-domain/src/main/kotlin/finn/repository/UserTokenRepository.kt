package finn.repository

import finn.entity.UserToken
import java.util.*

interface UserTokenRepository {

    fun save(
        userId: UUID, deviceId: UUID, deviceType: String, tokenValue: String,
        expiredAt: Date, issuedAt: Date
    )

    fun findByDeviceId(deviceId: UUID): UserToken

    fun updateRefreshToken(refreshToken: String, deviceId: UUID): Boolean

    fun deleteRefreshToken(deviceId: UUID)
}