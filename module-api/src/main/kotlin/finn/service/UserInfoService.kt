package finn.service

import finn.entity.UserInfo
import finn.entity.UserRole
import finn.entity.UserStatus
import finn.repository.UserInfoRepository
import finn.userinfo.createUniqueNickname
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserInfoService(
    private val userInfoRepository: UserInfoRepository
) {

    fun createUserInfo(oAuthUserId: UUID): UserInfo {
        val nickname = createUniqueNickname()
        return userInfoRepository.save(
            oAuthUserId,
            nickname,
            UserRole.USER.name,
            UserStatus.REGISTERED.name
        )
    }
}