package finn.entity

import java.util.*

class OAuthUser private constructor(
    val id: UUID,
    val provider: String,
    val providerId: String,
    val email: String,
) {

    companion object {
        fun create(id: UUID, provider: String, providerId: String, email: String): OAuthUser {
            return OAuthUser(id, provider, providerId, email)
        }
    }
}