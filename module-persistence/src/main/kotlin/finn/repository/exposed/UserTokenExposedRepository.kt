package finn.repository.exposed

import finn.entity.UserTokenExposed
import finn.exception.AuthenticationCriticalProblemException
import finn.exception.CriticalDataPollutedException
import finn.table.UserTokenTable
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class UserTokenExposedRepository() {

    companion object {
        private val log = KotlinLogging.logger {}
    }

    fun findByDeviceId(deviceId: UUID): String {
        return UserTokenExposed.find { UserTokenTable.deviceId eq deviceId }
            .singleOrNull()?.refreshToken
            ?: run {
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