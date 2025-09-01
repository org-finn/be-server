package finn.repository.exposed

import finn.entity.ArticleExposed
import finn.insertDto.ArticleToInsert
import finn.paging.PageResponse
import finn.queryDto.ArticleDataQueryDto
import finn.table.ArticleTable
import finn.table.ArticleTickerTable
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
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
        val tickerId: UUID,
        val headline: String,
        val sentiment: String?,
        val reasoning: String?
    ) : ArticleDataQueryDto {
        override fun articleId(): UUID = this.articleId
        override fun tickerId(): UUID = this.tickerId
        override fun headline(): String = this.headline
        override fun sentiment(): String? = this.sentiment
        override fun reasoning(): String? = this.reasoning
    }

    fun findArticleListByTickerId(tickerId: UUID): List<ArticleDataQueryDto> {
        return ArticleTickerTable.select(
            ArticleTickerTable.articleId,
            ArticleTickerTable.tickerId,
            ArticleTickerTable.title,
            ArticleTickerTable.sentiment,
            ArticleTickerTable.reasoning
        ).where(ArticleTickerTable.tickerId eq tickerId)
            .limit(3)
            .map { row ->
                ArticleDataQueryDtoImpl(
                    articleId = row[ArticleTickerTable.articleId],
                    tickerId = row[ArticleTickerTable.tickerId],
                    headline = row[ArticleTickerTable.title],
                    sentiment = row[ArticleTickerTable.sentiment],
                    reasoning = row[ArticleTickerTable.reasoning]
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