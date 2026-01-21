package finn.entity

import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.*

class AccessToken private constructor(
    val subject: UUID,
    val role: String,
    val status: String,
    val issuedAt: Date,
    val expiresAt: Date
) {

    companion object {
        val log = KotlinLogging.logger { }
        fun create(
            subject: String,
            role: String,
            status: String,
            issuedAt: Date,
            expiresAt: Date
        ): AccessToken {

            return AccessToken(
                UUID.fromString(subject),
                role,
                status,
                issuedAt,
                expiresAt
            )
        }


    }

}
