package finn.service

import finn.entity.UserInfo
import finn.entity.UserRole
import finn.entity.UserStatus
import finn.repository.UserInfoRepository
import finn.userinfo.NicknameProvider
import finn.userinfo.NicknameValidator
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserInfoService(
    private val userInfoRepository: UserInfoRepository,
    private val nicknameProvider: NicknameProvider,
    private val nicknameValidator: NicknameValidator
) {

    fun createUserInfo(oAuthUserId: UUID): UserInfo {
        val nickname = nicknameProvider.createUniqueNickname()
        return userInfoRepository.save(
            oAuthUserId,
            nickname,
            UserRole.USER.name,
            UserStatus.REGISTERED.name
        )
    }

    fun checkNicknameValidation(nickname: String): Boolean {
        nicknameValidator.isValid(nickname)
        return userInfoRepository.existNickname(nickname)
    }
}