package finn.repository.exposed

import finn.entity.ArticleExposed
import finn.exception.CriticalDataOmittedException
import finn.paging.PageResponse
import finn.queryDto.ArticleDataQueryDto
import finn.queryDto.ArticleDetailQueryDto
import finn.queryDto.ArticleDetailTickerQueryDto
import finn.table.ArticleTable
import finn.table.ArticleTickerTable
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.springframework.stereotype.Repository
import java.time.ZoneId
import java.time.ZonedDateTime
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
            ArticleTickerTable.titleKr,
            ArticleTickerTable.sentiment,
            ArticleTickerTable.reasoning,
            ArticleTickerTable.reasoningKr
        ).where(ArticleTickerTable.tickerId eq tickerId)
            .orderBy(ArticleTickerTable.publishedDate, SortOrder.DESC)
            .limit(3)
            .map { row ->
                ArticleDataQueryDtoImpl(
                    articleId = row[ArticleTickerTable.articleId],
                    tickerId = row[ArticleTickerTable.tickerId],
                    headline = row[ArticleTickerTable.titleKr] ?: row[ArticleTickerTable.title],
                    sentiment = row[ArticleTickerTable.sentiment],
                    reasoning = row[ArticleTickerTable.reasoningKr]
                        ?: row[ArticleTickerTable.reasoning]
                )
            }
    }

    fun findAllArticleList(
        tickerCodes: List<String>?,
        sentiment: String?,
        page: Int,
        size: Int
    ): PageResponse<ArticleExposed> {
        // 1. 서브쿼리 정의: ArticleTickerTable에서 'articleId' 컬럼만 선택
        // 조건이 없다면 null 처리하여 쿼리를 아예 실행하지 않도록 함
        val subQuery = if (tickerCodes != null || sentiment != null) {
            var query = ArticleTickerTable.select(ArticleTickerTable.articleId)

            if (tickerCodes != null) {
                query = query.andWhere { ArticleTickerTable.tickerCode inList tickerCodes }
            }

            if (sentiment != null) {
                query = query.andWhere { ArticleTickerTable.sentiment eq sentiment }
            }

            query.map { row -> row[ArticleTickerTable.articleId] }
                .toList()
        } else {
            null
        }

        val mainQuery = ArticleTable.selectAll()

        if (subQuery != null) {
            mainQuery.andWhere { ArticleTable.id inList subQuery }
        }

        val limit = size
        val offset = (page * limit).toLong()
        val itemsToFetch = limit + 1

        val results = mainQuery
            .orderBy(
                ArticleTable.publishedDate to SortOrder.DESC,
                ArticleTable.distinctId to SortOrder.ASC
            )
            .limit(itemsToFetch, offset)
            .map { ArticleExposed.wrapRow(it) }

        val hasNext = results.size > limit
        val content = if (hasNext) results.dropLast(1) else results

        // PageResponse 반환
        return PageResponse(content, page, size, hasNext)
    }

    private data class ArticleDetailQueryDtoImpl(
        val articleId: UUID,
        val headline: String,
        val description: String,
        val thumbnailUrl: String?,
        val contentUrl: String,
        val publishedDate: ZonedDateTime,
        val source: String,
        val tickers: List<ArticleDetailTickerQueryDtoImpl>?
    ) : ArticleDetailQueryDto {
        override fun articleId(): UUID = this.articleId

        override fun headline(): String = this.headline

        override fun description(): String = this.description

        override fun thumbnailUrl(): String? = this.thumbnailUrl

        override fun contentUrl(): String = this.contentUrl

        override fun publishedDate(): ZonedDateTime = this.publishedDate

        override fun source(): String = this.source

        override fun tickers(): List<ArticleDetailTickerQueryDtoImpl>? = this.tickers
    }

    private data class ArticleDetailTickerQueryDtoImpl(
        val shortCompanyName: String,
        val sentiment: String?,
        val reasoning: String?
    ) : ArticleDetailTickerQueryDto {
        override fun shortCompanyName(): String = this.shortCompanyName

        override fun sentiment(): String? = this.sentiment

        override fun reasoning(): String? = this.reasoning
    }

    fun findArticleDetailById(articleId: UUID): ArticleDetailQueryDto {
        val article = ArticleTable.selectAll()
            .where { ArticleTable.id eq articleId }
            .singleOrNull()

        val tickers = ArticleTickerTable.select(
            ArticleTickerTable.shortCompanyName,
            ArticleTickerTable.sentiment,
            ArticleTickerTable.reasoning,
            ArticleTickerTable.reasoningKr
        )
            .where { ArticleTickerTable.articleId eq articleId }
            .map { row ->
                ArticleDetailTickerQueryDtoImpl(
                    shortCompanyName = row[ArticleTickerTable.shortCompanyName],
                    sentiment = row[ArticleTickerTable.sentiment],
                    reasoning = row[ArticleTickerTable.reasoningKr]
                        ?: row[ArticleTickerTable.reasoning]
                )
            }.toList()

        return article?.let { row ->
            ArticleDetailQueryDtoImpl(
                articleId = row[ArticleTable.id].value,
                headline = row[ArticleTable.titleKr] ?: row[ArticleTable.title],
                description = row[ArticleTable.descriptionKr] ?: row[ArticleTable.description],
                thumbnailUrl = row[ArticleTable.thumbnailUrl],
                contentUrl = row[ArticleTable.articleUrl],
                publishedDate = row[ArticleTable.publishedDate].atZone(ZoneId.of("Asia/Seoul")), // KST 기준 적용
                source = row[ArticleTable.author],
                tickers = tickers
            )
        } ?: throw CriticalDataOmittedException("해당 articleId에 해당하는 아티클이 존재하지 않습니다.")
    }


}