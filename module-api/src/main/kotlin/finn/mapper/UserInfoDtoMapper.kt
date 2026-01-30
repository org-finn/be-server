package finn.mapper

import finn.entity.UserInfo
import finn.response.userinfo.UserInfoResponse
import org.springframework.stereotype.Component

@Component
class UserInfoDtoMapper {

    companion object {
        fun toDto(userInfo: UserInfo): UserInfoResponse {
            return UserInfoResponse(userInfo.nickname, userInfo.imageUrl)
        }
    }

}