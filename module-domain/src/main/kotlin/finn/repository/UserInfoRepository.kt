package finn.repository

import finn.entity.UserInfo
import java.util.*

interface UserInfoRepository {
    fun findById(userInfoId: UUID): UserInfo
}