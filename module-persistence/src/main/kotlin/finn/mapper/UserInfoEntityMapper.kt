package finn.mapper

import finn.entity.UserInfo
import finn.entity.UserInfoExposed

fun toDomain(userInfo: UserInfoExposed): UserInfo {
    return UserInfo.create(userInfo.id.value, userInfo.nickname, userInfo.role, userInfo.status)
}