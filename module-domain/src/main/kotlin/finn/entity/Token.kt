package finn.entity

import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.*

class Token private constructor(
    val subject: UUID,
    val role: String?,
    val status: String?,
    val deviceId: UUID?,
    val issuedAt: Date,
    val expiresAt: Date
) {

    companion object {
        val log = KotlinLogging.logger { }
        fun create(
            subject: String,
            role: String?,
            status: String?,
            deviceId: String?,
            issuedAt: Date,
            expiresAt: Date
        ): Token {

            return Token(
                UUID.fromString(subject),
                role,
                status,
                deviceId?.let { UUID.fromString(it) },
                issuedAt,
                expiresAt
            )
        }
    }

}
