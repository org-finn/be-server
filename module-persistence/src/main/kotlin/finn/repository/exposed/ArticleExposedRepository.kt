package finn.repository.exposed

import finn.exception.NotFoundDataException
import finn.paging.PageResponse
import finn.queryDto.ArticleDataQueryDto
import finn.queryDto.ArticleDetailQueryDto
import finn.queryDto.ArticleDetailTickerQueryDto
import finn.queryDto.PredictionArticleDataQueryDto
import finn.table.ArticleTable
import finn.table.ArticleTickerTable
import finn.table.UserArticleTable
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.stereotype.Repository
import java.time.ZoneId
import java.util.*

@Repository
class ArticleExposedRepository {
    companion object {
        private val log = KotlinLogging.logger {}
    }

    fun findArticleListByTickerId(tickerId: UUID): List<PredictionArticleDataQueryDto> {
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
                PredictionArticleDataQueryDto(
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
        userId: UUID?,
        tickerCodes: List<String>?,
        sentiment: String?,
        page: Int,
        size: Int
    ): PageResponse<ArticleDataQueryDto> {

        // 1. Join 테이블 구성: userId가 있으면 북마크 여부 조회를 위해 UserArticleTable과 Left Join
        val targetTable = if (userId != null) {
            Join(
                table = ArticleTable,
                otherTable = UserArticleTable,
                joinType = JoinType.LEFT,
                onColumn = ArticleTable.id,
                otherColumn = UserArticleTable.articleId,
                additionalConstraint = { UserArticleTable.userId eq userId }
            )
        } else {
            ArticleTable
        }

        val query = targetTable.selectAll()

        // 2. 필터 조건 적용: EXISTS 서브쿼리 활용 (애플리케이션 메모리에 UUID를 올리지 않고 DB 단에서 처리)
        if (!tickerCodes.isNullOrEmpty() || sentiment != null) {
            val subQuery =
                ArticleTickerTable.selectAll()
                    .where { ArticleTickerTable.articleId eq ArticleTable.id }

            if (!tickerCodes.isNullOrEmpty()) {
                subQuery.andWhere { ArticleTickerTable.tickerCode inList tickerCodes }
            }

            if (sentiment != null) {
                subQuery.andWhere { ArticleTickerTable.sentiment eq sentiment }
            }

            query.andWhere { exists(subQuery) }
        }

        // 3. 페이징 설정
        val limit = size
        val offset = (page * limit).toLong()
        val itemsToFetch = limit + 1 // 다음 페이지 존재 여부 파악을 위해 1개 더 조회

        // 4. 쿼리 실행 및 DTO 직접 매핑 (DAO 래핑 생략으로 성능 최적화)
        val results = query
            .orderBy(
                ArticleTable.publishedDate to SortOrder.DESC,
                ArticleTable.distinctId to SortOrder.ASC
            )
            .limit(itemsToFetch, offset)
            .map { row ->
                // userId가 넘겨졌고, Left Join 결과 UserArticleTable의 id가 null이 아니라면 북마크 한 것으로 간주
                val isFavorite = userId?.let { row.getOrNull(UserArticleTable.id) != null }

                ArticleDataQueryDto.create(
                    id = row[ArticleTable.id].value,
                    title = row[ArticleTable.titleKr] ?: row[ArticleTable.title],
                    description = row[ArticleTable.description],
                    thumbnailUrl = row[ArticleTable.thumbnailUrl],
                    contentUrl = row[ArticleTable.articleUrl],
                    // Exposed의 timestamp는 Instant를 반환하므로 시스템 요구사항(KST)에 맞춰 ZonedDateTime으로 변환
                    publishedDate = row[ArticleTable.publishedDate].atZone(ZoneId.of("Asia/Seoul")),
                    source = row[ArticleTable.author],
                    tickers = row[ArticleTable.tickers],
                    isFavorite = isFavorite
                )
            }

        // 5. PageResponse 구성
        val hasNext = results.size > limit
        val content = if (hasNext) results.dropLast(1) else results

        return PageResponse(content, page, size, hasNext)
    }


    fun findArticleDetailById(userId: UUID?, articleId: UUID): ArticleDetailQueryDto {
        val article = ArticleTable.selectAll()
            .where { ArticleTable.id eq articleId }
            .firstOrNull()

        val tickers = ArticleTickerTable.select(
            ArticleTickerTable.shortCompanyName,
            ArticleTickerTable.sentiment,
            ArticleTickerTable.reasoning,
            ArticleTickerTable.reasoningKr
        )
            .where { ArticleTickerTable.articleId eq articleId }
            .map { row ->
                ArticleDetailTickerQueryDto(
                    shortCompanyName = row[ArticleTickerTable.shortCompanyName],
                    sentiment = row[ArticleTickerTable.sentiment],
                    reasoning = row[ArticleTickerTable.reasoningKr]
                        ?: row[ArticleTickerTable.reasoning]
                )
            }.toList()

        var isFavorite = false
        if (userId != null) {
            isFavorite = !UserArticleTable.select(UserArticleTable.userId)
                .where { (UserArticleTable.userId eq userId) and (UserArticleTable.articleId eq articleId) }
                .limit(1).empty()

        }

        return article?.let { row ->
            ArticleDetailQueryDto(
                articleId = row[ArticleTable.id].value,
                headline = row[ArticleTable.titleKr] ?: row[ArticleTable.title],
                description = row[ArticleTable.descriptionKr] ?: row[ArticleTable.description],
                thumbnailUrl = row[ArticleTable.thumbnailUrl],
                contentUrl = row[ArticleTable.articleUrl],
                publishedDate = row[ArticleTable.publishedDate].atZone(ZoneId.of("Asia/Seoul")), // KST 기준 적용
                source = row[ArticleTable.author],
                tickers = tickers,
                isFavorite = isFavorite
            )
        } ?: throw NotFoundDataException("해당 articleId에 해당하는 아티클이 존재하지 않습니다.")
    }

    fun findArticleListByKeyword(keyword: String): List<ArticleDataQueryDto> {
        val searchResults = mutableListOf<ArticleDataQueryDto>()

        // title에서 찾기
        searchResults.addAll(
            ArticleTable.selectAll()
                .where { (ArticleTable.title like "%$keyword%") or (ArticleTable.titleKr like "%$keyword%") }
                .limit(30)
                .map { row ->
                    ArticleDataQueryDto.create(
                        id = row[ArticleTable.id].value,
                        title = row[ArticleTable.titleKr] ?: row[ArticleTable.title],
                        description = row[ArticleTable.description],
                        thumbnailUrl = row[ArticleTable.thumbnailUrl],
                        contentUrl = row[ArticleTable.articleUrl],
                        publishedDate = row[ArticleTable.publishedDate].atZone(ZoneId.of("Asia/Seoul")),
                        source = row[ArticleTable.author],
                        tickers = row[ArticleTable.tickers],
                        isFavorite = false
                    )
                }.toList()
        )

        // description에서 찾기
        searchResults.addAll(
            ArticleTable.selectAll()
                .where { (ArticleTable.description like "%$keyword%") or (ArticleTable.descriptionKr like "%$keyword%") }
                .limit(30)
                .map { row ->
                    ArticleDataQueryDto.create(
                        id = row[ArticleTable.id].value,
                        title = row[ArticleTable.titleKr] ?: row[ArticleTable.title],
                        description = row[ArticleTable.description],
                        thumbnailUrl = row[ArticleTable.thumbnailUrl],
                        contentUrl = row[ArticleTable.articleUrl],
                        publishedDate = row[ArticleTable.publishedDate].atZone(ZoneId.of("Asia/Seoul")),
                        source = row[ArticleTable.author],
                        tickers = row[ArticleTable.tickers],
                        isFavorite = false
                    )
                }.toList()
        )

        return searchResults
    }


}