package finn.repository.exposed

import finn.entity.UserInfoExposed
import finn.exception.CriticalDataPollutedException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class UserInfoExposedRepository {
    companion object {
        private val log = KotlinLogging.logger {}
    }

    fun findById(id: UUID): UserInfoExposed {
        return UserInfoExposed.findById(id)
            ?: run {
                log.error { "${id}의 userInfo가 존재하지 않습니다." }
                throw CriticalDataPollutedException("auth 관련 로직 중 문제가 발생하였습니다.")
            }
    }
}