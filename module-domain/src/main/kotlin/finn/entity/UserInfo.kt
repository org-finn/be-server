package finn.entity

import java.util.*

class UserInfo private constructor(
    val id: UUID,
    val nickname: String,
    val role: UserRole,
    val status: UserStatus,
) {

    companion object {
        fun create(id: UUID, nickname: String, role: UserRole, status: UserStatus): UserInfo {
            return UserInfo(id, nickname, role, status)
        }
    }
}