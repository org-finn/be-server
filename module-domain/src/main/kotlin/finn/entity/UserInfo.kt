package finn.entity

import java.util.*

class UserInfo private constructor(
    val id: UUID,
    val nickname: String,
    val imageUrl: String?,
    val role: UserRole,
    val status: UserStatus,
) {

    companion object {
        fun create(
            id: UUID,
            nickname: String,
            imageUrl: String?,
            role: String,
            status: String
        ): UserInfo {
            return UserInfo(
                id,
                nickname,
                imageUrl,
                UserRole.valueOf(role),
                UserStatus.valueOf(status)
            )
        }
    }
}