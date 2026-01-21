package finn.entity

import java.util.*

class RefreshToken private constructor(
    val tokenValue: String,
    val deviceId: UUID,
    val issuedAt: Date,
    val expiredAt: Date
) {

    companion object {
        fun create(
            tokenValue: String,
            deviceId: UUID,
            issuedAt: Date,
            expiresAt: Date
        ): RefreshToken {
            return RefreshToken(
                tokenValue,
                deviceId,
                issuedAt,
                expiresAt
            )
        }
    }

}