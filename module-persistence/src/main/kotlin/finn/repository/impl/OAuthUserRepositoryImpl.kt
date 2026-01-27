package finn.repository.impl

import finn.entity.UserInfo
import finn.mapper.toDomain
import finn.repository.OAuthUserRepository
import finn.repository.exposed.OAuthUserExposedRepository
import finn.repository.exposed.UserInfoExposedRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class OAuthUserRepositoryImpl(
    private val userInfoExposedRepository: UserInfoExposedRepository,
    private val oAuthUserExposedRepository: OAuthUserExposedRepository
) : OAuthUserRepository {

    override fun save(
        provider: String,
        providerId: String,
        email: String
    ): UUID {
        return oAuthUserExposedRepository.save(providerId, providerId, email)
    }

    override fun findByProviderId(providerId: String): UserInfo? {
        val userInfo = userInfoExposedRepository.findByProviderId(providerId)
        return userInfo?.let {
            toDomain(it)
        }
    }
}