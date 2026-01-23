package finn.entity

import java.util.*

class RefreshToken private constructor(
    val tokenValue: String,
    val issuedAt: Date,
    val expiredAt: Date
) {

    companion object {
        fun create(
            tokenValue: String,
            issuedAt: Date,
            expiredAt: Date
        ): RefreshToken {
            return RefreshToken(
                tokenValue,
                issuedAt,
                expiredAt
            )
        }
    }

}