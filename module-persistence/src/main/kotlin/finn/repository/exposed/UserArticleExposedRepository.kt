package finn.repository.exposed

import finn.queryDto.FavoriteArticleQueryDto
import finn.table.ArticleTable
import finn.table.UserArticleTable
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.springframework.stereotype.Repository
import java.time.Clock
import java.time.LocalDateTime
import java.util.*

@Repository
class UserArticleExposedRepository(
    private val clock: Clock,
) {

    fun findFavoriteArticlesByUserId(userId: UUID): List<FavoriteArticleQueryDto> {
        return UserArticleTable.join(
            ArticleTable,
            JoinType.INNER,
            UserArticleTable.articleId eq ArticleTable.id
        ).select(
            ArticleTable.id, ArticleTable.title, ArticleTable.thumbnailUrl
        ).where { UserArticleTable.userId eq userId }
            .map { row ->
                FavoriteArticleQueryDto(
                    articleId = row[ArticleTable.id].value,
                    title = row[ArticleTable.title],
                    thumbnailUrl = row[ArticleTable.thumbnailUrl]
                )
            }.toList()
    }

    fun addFavoriteArticle(userId: UUID, articleId: UUID) {
        // 1. 선 조회(이미 있으면 삽입 건너뜀)
        val isExist = !UserArticleTable.selectAll()
            .where {
                UserArticleTable.articleId eq articleId
                UserArticleTable.userId eq userId
            }.limit(1) // 찾으면 더 이상 조회하지 않음
            .empty()
        // 2. 생성
        if (!isExist) {
            UserArticleTable.insert {
                it[UserArticleTable.articleId] = articleId
                it[UserArticleTable.userId] = userId
                it[UserArticleTable.createdAt] = LocalDateTime.now(clock)
            }
        }
    }

    fun removeFavoriteArticle(userId: UUID, articleId: UUID) {
        // 1. 선 조회(없으면 삭제 건너뜀)
        val isExist = !UserArticleTable.selectAll()
            .where {
                UserArticleTable.articleId eq articleId
                UserArticleTable.userId eq userId
            }.limit(1) // 찾으면 더 이상 조회하지 않음
            .empty()
        // 2. 삭제
        if (!isExist) {
            UserArticleTable.deleteWhere {
                UserArticleTable.articleId eq articleId
                UserArticleTable.userId eq userId
            }
        }
    }

}