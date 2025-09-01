package finn.repository.impl

import finn.entity.command.ArticleC
import finn.entity.command.ArticleInsight
import finn.entity.query.ArticleQ
import finn.exception.CriticalDataPollutedException
import finn.insertDto.ArticleToInsert
import finn.mapper.toDomain
import finn.paging.PageResponse
import finn.queryDto.ArticleDataQueryDto
import finn.repository.ArticleRepository
import finn.repository.exposed.ArticleExposedRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class ArticleRepositoryImpl(
    private val articleExposedRepository: ArticleExposedRepository
) : ArticleRepository {
    override fun getArticleDataForPredictionDetail(tickerId: UUID): List<ArticleDataQueryDto> {
        return articleExposedRepository.findArticleListByTickerId(tickerId)
    }

    override fun getArticleList(
        page: Int,
        size: Int,
        filter: String,
        sort: String
    ): PageResponse<ArticleQ> {
        val ArticleExposedList = when (filter) {
            "all" -> articleExposedRepository.findAllArticleList(page, size)

            else -> throw CriticalDataPollutedException("filter: $filter, 지원하지 않는 옵션입니다.")
        }
        return PageResponse(ArticleExposedList.content.map { it ->
            toDomain(it)
        }.toList(), page, size, ArticleExposedList.hasNext)
    }

    override fun saveArticle(article: ArticleC, insights: List<ArticleInsight>) : UUID {
        val articleToInsert = ArticleToInsert(
            article.title, article.description, article.thumbnailUrl, article.contentUrl,
            article.publishedDate, article.source, article.distinctId,
            article.tickers?.joinToString(",")
        )
        return articleExposedRepository.save(articleToInsert)
    }
}