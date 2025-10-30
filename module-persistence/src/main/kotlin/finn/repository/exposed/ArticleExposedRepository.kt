package finn.repository.exposed

import finn.entity.ArticleExposed
import finn.exception.CriticalDataOmittedException
import finn.insertDto.ArticleToInsert
import finn.paging.PageResponse
import finn.queryDto.ArticleDataQueryDto
import finn.queryDto.ArticleDetailQueryDto
import finn.queryDto.ArticleDetailTickerQueryDto
import finn.table.ArticleTable
import finn.table.ArticleTickerTable
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
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
            ArticleTickerTable.sentiment,
            ArticleTickerTable.reasoning
        ).where(ArticleTickerTable.tickerId eq tickerId)
            .orderBy(ArticleTickerTable.publishedDate, SortOrder.DESC)
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
        tickerCodes: List<String>?,
        sentiment: String?,
        page: Int,
        size: Int
    ): PageResponse<ArticleExposed> {
        val limit = size
        val offset = (page * limit).toLong()
        val itemsToFetch = limit + 1

        // 기본 쿼리 생성
        var query = if (tickerCodes == null && sentiment == null) {
            ArticleTable.selectAll()
        } else {
            ArticleTable.join(
                ArticleTickerTable, JoinType.INNER,
                ArticleTable.id, ArticleTickerTable.articleId
            ).selectAll()
        }

        // tickerCodes가 null이 아닐 경우에만 where 조건 추가
        if (tickerCodes != null) {
            query = query.andWhere { ArticleTickerTable.tickerCode inList tickerCodes }
        }

        // sentiment가 Null이 아닐 경우에만 where 조건 추가
        if (sentiment != null) {
            query = query.andWhere { ArticleTickerTable.sentiment eq sentiment }
        }

        val results = query.orderBy(ArticleTable.publishedDate, SortOrder.DESC)
            .limit(itemsToFetch, offset)
            .map { ArticleExposed.wrapRow(it) }
        val hasNext = results.size > limit
        val content = if (hasNext) results.dropLast(1) else results

        return PageResponse(
            content = content,
            page = page,
            size = content.size,
            hasNext = hasNext
        )
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
            ArticleTickerTable.sentiment, ArticleTickerTable.reasoning
        )
            .where { ArticleTickerTable.articleId eq articleId }
            .map { row ->
                ArticleDetailTickerQueryDtoImpl(
                    shortCompanyName = row[ArticleTickerTable.shortCompanyName],
                    sentiment = row[ArticleTickerTable.sentiment],
                    reasoning = row[ArticleTickerTable.reasoning]
                )
            }.toList()

        return article?.let { row ->
            ArticleDetailQueryDtoImpl(
                articleId = row[ArticleTable.id].value,
                headline = row[ArticleTable.title],
                description = row[ArticleTable.description],
                thumbnailUrl = row[ArticleTable.thumbnailUrl],
                contentUrl = row[ArticleTable.articleUrl],
                publishedDate = row[ArticleTable.publishedDate].atZone(ZoneId.of("Asia/Seoul")), // KST 기준 적용
                source = row[ArticleTable.author],
                tickers = tickers
            )
        } ?: throw CriticalDataOmittedException("해당 articleId에 해당하는 아티클이 존재하지 않습니다.")
    }


    fun save(article: ArticleToInsert): UUID? {
        val insertedRowCount = ArticleTable.insertIgnore {
            it[publishedDate] = article.publishedDate.toInstant(ZoneOffset.UTC)
            it[title] = article.title
            it[description] = article.description
            it[articleUrl] = article.contentUrl
            it[thumbnailUrl] = article.thumbnailUrl
            it[viewCount] = 0L
            it[likeCount] = 0L
            it[author] = article.source
            it[distinctId] = article.distinctId
            it[tickers] = article.tickers
            it[createdAt] = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
        }
        if (insertedRowCount.insertedCount > 0) {
            log.debug { "새로운 아티클을 성공적으로 INSERT 하였습니다. distinctId: ${article.distinctId}" }
        } else {
            log.debug { "이미 존재하는 아티클이므로 INSERT를 건너뛰었습니다. distinctId: ${article.distinctId}" }
            return null
        }

        val articleId = ArticleTable
            .select(ArticleTable.id)
            .where { ArticleTable.distinctId eq article.distinctId }
            .singleOrNull()

        return articleId?.get(ArticleTable.id)?.value
            ?: throw CriticalDataOmittedException("${article.distinctId}의 데이터가 DB에 존재하지 않습니다.")
    }

}