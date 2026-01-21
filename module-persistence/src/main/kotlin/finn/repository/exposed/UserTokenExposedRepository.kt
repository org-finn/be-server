package finn.repository.exposed

import finn.entity.UserTokenExposed
import finn.exception.AuthenticationCriticalProblemException
import finn.exception.CriticalDataPollutedException
import finn.table.UserTokenTable
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
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
    }

    fun save(
        userInfoId: UUID,
        deviceId: UUID,
        deviceType: String,
        refreshToken: String,
        expiresAt: Date,
        issuedAt: Date
    ) {
        UserTokenExposed.new {
            this.userInfoId = userInfoId
            this.deviceId = deviceId
            this.deviceType = deviceType
            this.refreshToken = refreshToken
            this.expiredAt = expiresAt.toInstant().atZone(clock.zone).toLocalDateTime()
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

    fun update(refreshToken: String, deviceId: UUID) {
        UserTokenExposed.findSingleByAndUpdate(UserTokenTable.deviceId eq deviceId) {
            it.refreshToken = refreshToken
        } ?: run {
            log.error { "${deviceId}의 user_token이 존재하지 않아 업데이트에 실패했습니다." }
            throw CriticalDataPollutedException("${deviceId}의 user_token이 존재하지 않습니다.")
        }
    }

    fun delete(deviceId: UUID) {
        // 존재하지 않는 row를 지운다해도 굳이 예외를 터뜨리지 않음
        UserTokenExposed.find { UserTokenTable.deviceId eq deviceId }.singleOrNull()?.delete()
    }

}