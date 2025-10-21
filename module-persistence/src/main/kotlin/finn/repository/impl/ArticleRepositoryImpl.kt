package finn.repository.impl

import finn.entity.command.ArticleC
import finn.entity.command.ArticleInsight
import finn.entity.query.ArticleQ
import finn.insertDto.ArticleToInsert
import finn.mapper.toDomain
import finn.paging.PageResponse
import finn.queryDto.ArticleDataQueryDto
import finn.queryDto.ArticleDetailQueryDto
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
        tickerCodes: List<String>?,
        sentiment: String?,
        sort: String
    ): PageResponse<ArticleQ> {
        val articleExposedList =
            articleExposedRepository.findAllArticleList(tickerCodes, sentiment, page, size)
        return PageResponse(articleExposedList.content.map {
            toDomain(it)
        }.toList(), page, size, articleExposedList.hasNext)
    }

    override fun getArticle(articleId: UUID): ArticleDetailQueryDto {
        return articleExposedRepository.findArticleDetailById(articleId)
    }

    override fun saveArticle(article: ArticleC, insights: List<ArticleInsight>): UUID? {
        val articleToInsert = ArticleToInsert(
            article.title, article.description, article.thumbnailUrl, article.contentUrl,
            article.publishedDate, article.source, article.distinctId,
            article.tickers?.joinToString(",")
        )
        return articleExposedRepository.save(articleToInsert)
    }
}