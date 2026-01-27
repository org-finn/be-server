package finn.entity

import java.time.LocalDateTime
import java.util.*

class UserToken private constructor(
    val userId: UUID,
    val deviceId: UUID,
    val deviceType: String,
    val refreshToken: String?,
    val createdAt: LocalDateTime,
) {

    companion object {
        fun create(
            userId: UUID,
            deviceId: UUID,
            deviceType: String,
            refreshToken: String?,
            createdAt: LocalDateTime,
        ): UserToken {
            return UserToken(
                userId,
                deviceId,
                deviceType,
                refreshToken,
                createdAt
            )
        }
    }
}