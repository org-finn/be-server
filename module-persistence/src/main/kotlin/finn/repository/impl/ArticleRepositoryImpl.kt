package finn.repository.impl

import finn.entity.query.ArticleQ
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

}