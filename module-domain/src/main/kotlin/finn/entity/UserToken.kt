package finn.entity

import java.time.LocalDateTime
import java.util.*

class UserToken private constructor(
    val userId: UUID,
    val deviceId: UUID,
    val deviceType: String,
    val refreshToken: String,
    val expiredAt: Date,
    val issuedAt: Date,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {

    companion object {
        fun create(
            userId: UUID,
            deviceId: UUID,
            deviceType: String,
            refreshToken: String,
            expiredAt: Date,
            issuedAt: Date,
            createdAt: LocalDateTime,
            updatedAt: LocalDateTime,
        ): UserToken {
            return UserToken(
                userId,
                deviceId,
                deviceType,
                refreshToken,
                expiredAt,
                issuedAt,
                createdAt,
                updatedAt
            )
        }
    }
}