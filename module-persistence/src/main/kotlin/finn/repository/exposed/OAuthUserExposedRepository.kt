package finn.repository.exposed

import finn.entity.OAuthUserExposed
import org.springframework.stereotype.Repository
import java.time.Clock
import java.time.LocalDateTime
import java.util.*

@Repository
class OAuthUserExposedRepository(
    private val clock: Clock,
) {

    fun save(provider: String, providerId: String, email: String): UUID {
        return OAuthUserExposed.new {
            this.provider = provider
            this.providerId = providerId
            this.email = email
            this.createdAt = LocalDateTime.now(clock)
            this.updatedAt = LocalDateTime.now(clock)
        }.id.value
    }
}