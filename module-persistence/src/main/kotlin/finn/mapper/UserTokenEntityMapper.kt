package finn.mapper

import finn.entity.UserToken
import finn.entity.UserTokenExposed

fun toDomain(userToken: UserTokenExposed): UserToken {
    return UserToken.create(
        userToken.userInfoId,
        userToken.deviceId,
        userToken.deviceType,
        userToken.refreshToken,
        userToken.createdAt
    )
}