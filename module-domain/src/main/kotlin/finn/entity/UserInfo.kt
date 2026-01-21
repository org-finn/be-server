package finn.entity

import java.util.*

class UserInfo private constructor(
    val id: UUID,
    val nickname: String,
    val role: UserRole,
    val status: UserStatus,
) {

    companion object {
        fun create(id: UUID, nickname: String, role: String, status: String): UserInfo {
            return UserInfo(id, nickname, UserRole.valueOf(role), UserStatus.valueOf(status))
        }
    }
}