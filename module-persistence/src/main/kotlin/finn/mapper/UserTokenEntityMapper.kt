package finn.mapper

import finn.entity.UserToken
import finn.entity.UserTokenExposed
import java.sql.Date

fun toDomain(userToken: UserTokenExposed): UserToken {
    return UserToken.create(
        userToken.userInfoId,
        userToken.deviceId,
        userToken.deviceType,
        userToken.refreshToken,
        Date.valueOf(userToken.expiredAt.toString()),
        Date.valueOf(userToken.createdAt.toString()),
        userToken.createdAt,
        userToken.updatedAt
    )
}