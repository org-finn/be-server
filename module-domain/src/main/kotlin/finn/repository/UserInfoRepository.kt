package finn.repository

import finn.entity.UserInfo
import java.util.*

interface UserInfoRepository {
    fun save(oAuthUserId: UUID, nickname: String, role: String, status: String) : UserInfo

    fun findById(userInfoId: UUID): UserInfo

    fun existNickname(nickname: String): Boolean

    fun updateNickname(nickname: String, userId: UUID)
}