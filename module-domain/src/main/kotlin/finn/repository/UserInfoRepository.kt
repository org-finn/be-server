package finn.repository

import finn.entity.UserInfo
import finn.queryDto.FavoriteArticleQueryDto
import finn.queryDto.FavoriteTickerQueryDto
import java.util.*

interface UserInfoRepository {
    fun save(
        oAuthUserId: UUID,
        nickname: String,
        imageUrl: String?,
        role: String,
        status: String
    ): UserInfo

    fun findById(userInfoId: UUID): UserInfo

    fun existNickname(nickname: String): Boolean

    fun updateNickname(nickname: String, userId: UUID)

    fun findFavoriteTickers(userId: UUID): List<FavoriteTickerQueryDto>

    fun updateFavoriteTickers(userId: UUID, tickerCodes: List<String>)

    fun updateFavoriteTicker(userId: UUID, tickerCode: String, mode: String)

    fun deleteUserInfo(userId: UUID, status: String)

    fun findFavoriteArticles(userId: UUID): List<FavoriteArticleQueryDto>

    fun updateFavoriteArticle(userId: UUID, articleId: UUID, mode: String)
}