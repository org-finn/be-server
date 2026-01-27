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
        private val REFRESH_TOKEN_MAX_COUNT = 5
    }

    fun save(
        userInfoId: UUID,
        deviceId: UUID,
        deviceType: String,
        refreshToken: String,
        expiredAt: Date,
        issuedAt: Date
    ) {
        val curCount = UserTokenTable.selectAll()
            .where { UserTokenTable.userInfoId eq userInfoId }
            .count()

        // 기존 제한 개수를 초과하는 경우 제일 오래된 만료 시간의 토큰을 제거함
        if (curCount > REFRESH_TOKEN_MAX_COUNT) {
            val oldestTokenId = UserTokenTable.select(UserTokenTable.id)
                .where { UserTokenTable.userInfoId eq userInfoId }
                .orderBy(UserTokenTable.expiredAt, SortOrder.ASC)
                .limit(1)
                .map { it[UserTokenTable.id].value }
                .singleOrNull()
            if (oldestTokenId == null) {
                log.error { "${userInfoId}의 user_token이 ${curCount}개 존재하지만 다시 조회 시 오류가 발생했습니다." }
                throw AuthenticationCriticalProblemException("auth 관련 로직 중 문제가 발생하였습니다.")
            }
            deleteById(oldestTokenId)
        }

        UserTokenExposed.new {
            this.userInfoId = userInfoId
            this.deviceId = deviceId
            this.deviceType = deviceType
            this.refreshToken = refreshToken
            this.expiredAt = expiredAt.toInstant().atZone(clock.zone).toLocalDateTime()
            this.issuedAt = issuedAt.toInstant().atZone(clock.zone).toLocalDateTime()
            this.createdAt = LocalDateTime.now(clock)
            this.updatedAt = LocalDateTime.now(clock)
        }
    }

    fun findByDeviceId(deviceId: UUID): UserTokenExposed {
        return UserTokenExposed.find { UserTokenTable.deviceId eq deviceId }
            .singleOrNull() ?: run {
            log.error { "${deviceId}의 user_token이 존재하지 않습니다." }
            throw AuthenticationCriticalProblemException("auth 관련 로직 중 문제가 발생하였습니다.")
        }
    }

    fun update(refreshToken: String, deviceId: UUID, issuedAt: Date, expiredAt: Date): Boolean {
        UserTokenExposed.findSingleByAndUpdate(UserTokenTable.deviceId eq deviceId) {
            it.refreshToken = refreshToken
            it.expiredAt = expiredAt.toInstant().atZone(clock.zone).toLocalDateTime()
            it.issuedAt = issuedAt.toInstant().atZone(clock.zone).toLocalDateTime()
            it.updatedAt = LocalDateTime.now(clock)
        }?.let { return true }
        return false
    }

    fun release(deviceId: UUID) {
        UserTokenExposed.findSingleByAndUpdate(UserTokenTable.deviceId eq deviceId) {
            it.refreshToken = null
            it.issuedAt = null
            it.expiredAt = null
            it.updatedAt = LocalDateTime.now(clock)
        }
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