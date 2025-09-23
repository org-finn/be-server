package finn.repository.exposed

import finn.entity.ArticleExposed
import finn.exception.CriticalDataOmittedException
import finn.insertDto.ArticleToInsert
import finn.paging.PageResponse
import finn.queryDto.ArticleDataQueryDto
import finn.table.ArticleTable
import finn.table.ArticleTickerTable
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.sql.*
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
        tickerId: UUID?,
        sentiment: String?,
        page: Int,
        size: Int
    ): PageResponse<ArticleExposed> {
        val limit = size
        val offset = (page * limit).toLong()
        val itemsToFetch = limit + 1

        // 기본 쿼리 생성
        var query = if (tickerId == null && sentiment == null) {
            ArticleTable.selectAll()
        } else {
            ArticleTable.join(
                ArticleTickerTable, JoinType.INNER,
                ArticleTable.id, ArticleTickerTable.articleId
            ).selectAll()
        }

        // tickerId가 Null이 아닐 경우에만 where 조건 추가
        if (tickerId != null) {
            query = query.andWhere { ArticleTickerTable.tickerId eq tickerId }
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


    fun save(article: ArticleToInsert): UUID? {
        val insertedRowCount = ArticleTable.insertIgnore {
            it[publishedDate] = article.publishedDate
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