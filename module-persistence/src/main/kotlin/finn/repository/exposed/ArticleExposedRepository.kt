package finn.repository.exposed

import finn.entity.ArticleExposed
import finn.insertDto.ArticleToInsert
import finn.paging.PageResponse
import finn.queryDto.ArticleDataQueryDto
import finn.table.ArticleTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.batchInsert
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Repository
class ArticleExposedRepository {

    private data class ArticleDataQueryDtoImpl(
        val articleId: UUID,
        val headline: String,
        val sentiment: String?,
        val reasoning: String?
    ) : ArticleDataQueryDto {
        override fun articleId(): UUID = this.articleId
        override fun headline(): String = this.headline
        override fun sentiment(): String? = this.sentiment
        override fun reasoning(): String? = this.reasoning
    }

    fun findArticleListByTickerId(tickerId: UUID): List<ArticleDataQueryDto> {
        return ArticleTable.select(
            ArticleTable.id,
            ArticleTable.title,
            ArticleTable.sentiment,
            ArticleTable.reasoning
        ).where(ArticleTable.tickerId eq tickerId)
            .orderBy(ArticleTable.publishedDate to SortOrder.DESC)
            .limit(3)
            .map { row ->
                ArticleDataQueryDtoImpl(
                    articleId = row[ArticleTable.id].value,
                    headline = row[ArticleTable.title],
                    sentiment = row[ArticleTable.sentiment],
                    reasoning = row[ArticleTable.reasoning]
                )
            }
    }

    fun findAllArticleList(
        page: Int,
        size: Int
    ): PageResponse<ArticleExposed> {
        val limit = size
        val offset = (page * limit).toLong()
        val itemsToFetch = limit + 1

        val results = ArticleExposed.all()
            .orderBy(ArticleTable.publishedDate to SortOrder.DESC)
            .limit(itemsToFetch, offset)
            .toList()
        val hasNext = results.size > limit
        val content = if (hasNext) results.dropLast(1) else results

        return PageResponse(
            content = content,
            page = page,
            size = content.size,
            hasNext = hasNext
        )
    }

    fun findAllPositiveArticleList(
        page: Int,
        size: Int
    ): PageResponse<ArticleExposed> {
        val limit = size
        val offset = (page * limit).toLong()
        val itemsToFetch = limit + 1

        val results = ArticleExposed.find(ArticleTable.sentiment eq "positive")
            .orderBy(ArticleTable.publishedDate to SortOrder.DESC)
            .limit(itemsToFetch, offset)
            .toList()
        val hasNext = results.size > limit
        val content = if (hasNext) results.dropLast(1) else results

        return PageResponse(
            content = content,
            page = page,
            size = content.size,
            hasNext = hasNext
        )
    }

    fun findAllNegativeArticleList(
        page: Int,
        size: Int
    ): PageResponse<ArticleExposed> {
        val limit = size
        val offset = (page * limit).toLong()
        val itemsToFetch = limit + 1

        val results = ArticleExposed.find(ArticleTable.sentiment eq "negative")
            .orderBy(ArticleTable.publishedDate to SortOrder.DESC)
            .limit(itemsToFetch, offset)
            .toList()
        val hasNext = results.size > limit
        val content = if (hasNext) results.dropLast(1) else results

        return PageResponse(
            content = content,
            page = page,
            size = content.size,
            hasNext = hasNext
        )
    }

    fun saveAll(articleList: List<ArticleToInsert>) {
        ArticleTable.batchInsert(
            articleList,
            ignore = true // distinctId unique 조건 위반 데이터는 삽입 건너뜀
        ) { article ->
            this[ArticleTable.publishedDate] = article.publishedDate
            this[ArticleTable.title] = article.title
            this[ArticleTable.description] = article.description
            this[ArticleTable.articleUrl] = article.contentUrl
            this[ArticleTable.thumbnailUrl] = article.thumbnailUrl
            this[ArticleTable.viewCount] = 0L
            this[ArticleTable.likeCount] = 0L
            this[ArticleTable.sentiment] = article.sentiment
            this[ArticleTable.reasoning] = article.reasoning
            this[ArticleTable.shortCompanyName] = article.shortCompanyName
            this[ArticleTable.author] = article.source
            this[ArticleTable.distinctId] = article.distinctId
            this[ArticleTable.tickerId] = article.tickerId
            this[ArticleTable.tickerCode] = article.tickerCode
            this[ArticleTable.createdAt] = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        }
    }

}