package finn.repository.query

import finn.entity.NewsExposed
import finn.paging.PageResponse
import finn.queryDto.NewsDataQueryDto
import finn.table.NewsTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class NewsQueryRepository {

    private data class NewsDataQueryDtoImpl(
        val newsId: UUID,
        val headline: String,
        val sentiment: String,
        val reasoning: String?
    ) : NewsDataQueryDto {
        override fun newsId(): UUID = this.newsId
        override fun headline(): String = this.headline
        override fun sentiment(): String = this.sentiment
        override fun reasoning(): String? = this.reasoning
    }

    fun findNewsListByTickerId(tickerId: UUID): List<NewsDataQueryDto> {
        return NewsTable.select(
            NewsTable.id,
            NewsTable.title,
            NewsTable.sentiment,
            NewsTable.reasoning
        ).where(NewsTable.tickerId eq tickerId)
            .orderBy(NewsTable.publishedDate to SortOrder.DESC)
            .limit(3)
            .map { row ->
                NewsDataQueryDtoImpl(
                    newsId = row[NewsTable.id].value,
                    headline = row[NewsTable.title],
                    sentiment = row[NewsTable.sentiment],
                    reasoning = row[NewsTable.reasoning]
                )
            }
    }

    fun findAllNewsList(
        page: Int,
        size: Int
    ): PageResponse<NewsExposed> {
        val limit = size
        val offset = (page * limit).toLong()
        val itemsToFetch = limit + 1

        val results = NewsExposed.all()
            .orderBy(NewsTable.publishedDate to SortOrder.DESC)
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

    fun findAllPositiveNewsList(
        page: Int,
        size: Int
    ): PageResponse<NewsExposed> {
        val limit = size
        val offset = (page * limit).toLong()
        val itemsToFetch = limit + 1

        val results = NewsExposed.find(NewsTable.sentiment eq "positive")
            .orderBy(NewsTable.publishedDate to SortOrder.DESC)
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

    fun findAllNegativeNewsList(
        page: Int,
        size: Int
    ): PageResponse<NewsExposed> {
        val limit = size
        val offset = (page * limit).toLong()
        val itemsToFetch = limit + 1

        val results = NewsExposed.find(NewsTable.sentiment eq "negative")
            .orderBy(NewsTable.publishedDate to SortOrder.DESC)
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

}