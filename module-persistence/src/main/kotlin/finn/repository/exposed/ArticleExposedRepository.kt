package finn.repository.exposed

import finn.entity.ArticleExposed
import finn.insertDto.ArticleToInsert
import finn.paging.PageResponse
import finn.queryDto.ArticleDataQueryDto
import finn.table.ArticleTable
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.sql.SortOrder
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Repository
class ArticleExposedRepository {
    companion object {
        private val log = KotlinLogging.logger {}
    }

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

    fun save(article: ArticleToInsert): UUID {
        val savedArticle = ArticleExposed.new {
            this.publishedDate = article.publishedDate
            this.title = article.title
            this.description = article.description
            this.contentUrl = article.contentUrl
            this.thumbnailUrl = article.thumbnailUrl
            this.viewCount = 0L
            this.likeCount = 0L
            this.author = article.source
            this.distinctId = article.distinctId
            this.tickers = article.tickers
            this.createdAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
        }
        log.debug { "id:${savedArticle.id.value}, 아티클 데이터를 성공적으로 저장하였습니다." }
        return savedArticle.id.value
    }

}