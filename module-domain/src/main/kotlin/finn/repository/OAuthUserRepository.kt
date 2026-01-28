package finn.repository

import finn.entity.UserInfo
import java.util.*

interface OAuthUserRepository {
    fun save(provider: String, providerId: String, email: String): UUID

    fun findByProviderId(providerId: String): UserInfo?
}