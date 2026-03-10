package finn.repository.exposed

import finn.entity.UserTokenExposed
import finn.exception.AuthenticationCriticalProblemException
import finn.table.UserTokenTable
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.springframework.stereotype.Repository
import java.time.Clock
import java.time.LocalDateTime
import java.util.*

@Repository
class UserTokenExposedRepository(
    private val clock: Clock
) {

    companion object {
        private val log = KotlinLogging.logger {}
        private const val REFRESH_TOKEN_MAX_COUNT = 5
    }

    fun save(
        userId: UUID,
        deviceId: UUID,
        deviceType: String,
        refreshToken: String,
    ) {
        val curCount = UserTokenTable.selectAll()
            .where { UserTokenTable.userInfoId eq userId }
            .count()

        // 기존 제한 개수를 초과하는 경우 제일 오래된 만료 시간의 토큰을 제거함
        if (curCount > REFRESH_TOKEN_MAX_COUNT) {
            val oldestTokenId = UserTokenTable.select(UserTokenTable.id)
                .where { UserTokenTable.userInfoId eq userId }
                .orderBy(UserTokenTable.createdAt, SortOrder.ASC)
                .limit(1)
                .map { it[UserTokenTable.id].value }
                .singleOrNull()
            if (oldestTokenId != null) {
                deleteById(oldestTokenId)
            }
        }

        UserTokenExposed.new {
            this.userInfoId = userId
            this.deviceId = deviceId
            this.deviceType = deviceType
            this.refreshToken = refreshToken
            this.createdAt = LocalDateTime.now(clock)
        }
    }

    fun findByDeviceId(deviceId: UUID): UserTokenExposed {
        return UserTokenExposed.find { UserTokenTable.deviceId eq deviceId }
            .singleOrNull() ?: run {
            log.error { "${deviceId}의 user_token이 존재하지 않습니다." }
            throw AuthenticationCriticalProblemException("auth 관련 로직 중 문제가 발생하였습니다.")
        }
    }

    fun update(
        deletingRefreshToken: String,
        refreshToken: String,
        userId: UUID,
        deviceId: UUID,
        deviceType: String
    ) {
        UserTokenExposed.find(UserTokenTable.refreshToken eq deletingRefreshToken)
            .singleOrNull()?.delete() // 만약 존재한다면 delete, 탈취 등으로 인해 존재하지 않는다면 skip

        save(userId, deviceId, deviceType, refreshToken)
    }

    fun release(deviceId: UUID) {
        UserTokenExposed.find(UserTokenTable.deviceId eq deviceId)
            .singleOrNull()?.delete() // 만약 존재한다면 delete, 탈취 등으로 인해 존재하지 않는다면 skip
    }

    fun deleteByDeviceId(deviceId: UUID) {
        // 존재하지 않는 row를 지운다해도 굳이 예외를 터뜨리지 않음
        UserTokenExposed.find { UserTokenTable.deviceId eq deviceId }.singleOrNull()?.delete()
    }

    fun deleteById(id: UUID) {
        // 존재하지 않는 row를 지운다해도 굳이 예외를 터뜨리지 않음
        UserTokenExposed.find { UserTokenTable.id eq id }.singleOrNull()?.delete()
    }
}