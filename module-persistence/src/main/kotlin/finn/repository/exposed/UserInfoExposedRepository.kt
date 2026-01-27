package finn.repository.exposed

import finn.entity.UserInfoExposed
import finn.exception.CriticalDataPollutedException
import finn.table.OAuthUserTable
import finn.table.UserInfoTable
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.selectAll
import org.springframework.stereotype.Repository
import java.time.Clock
import java.time.LocalDateTime
import java.util.*

@Repository
class UserInfoExposedRepository(
    private val clock: Clock,
) {
    companion object {
        private val log = KotlinLogging.logger {}
    }

    fun save(oAuthUserId: UUID, nickname: String, role: String, status: String): UserInfoExposed {
        return UserInfoExposed.new {
            this.oauthUserId = oAuthUserId
            this.nickname = nickname
            this.role = role
            this.status = status
            this.createdAt = LocalDateTime.now(clock)
            this.updatedAt = LocalDateTime.now(clock)
        }
    }

    fun findById(id: UUID): UserInfoExposed {
        return UserInfoExposed.findById(id)
            ?: run {
                log.error { "${id}의 userInfo가 존재하지 않습니다." }
                throw CriticalDataPollutedException("auth 관련 로직 중 문제가 발생하였습니다.")
            }
    }

    fun findByProviderId(providerId: String): UserInfoExposed? {
        val row = UserInfoTable.join(
            OAuthUserTable,
            JoinType.INNER,
            UserInfoTable.oauthUserId,
            OAuthUserTable.id
        )
            .selectAll()
            .where { OAuthUserTable.providerId eq providerId }
            .singleOrNull()

        return row?.let { UserInfoExposed.wrapRow(it) }
    }
}