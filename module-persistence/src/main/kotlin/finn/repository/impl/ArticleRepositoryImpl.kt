package finn.repository.impl

import finn.paging.PageResponse
import finn.queryDto.ArticleDataQueryDto
import finn.queryDto.ArticleDetailQueryDto
import finn.queryDto.PredictionArticleDataQueryDto
import finn.repository.ArticleRepository
import finn.repository.exposed.ArticleExposedRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class ArticleRepositoryImpl(
    private val articleExposedRepository: ArticleExposedRepository
) : ArticleRepository {
    override fun getArticleDataForPredictionDetail(tickerId: UUID): List<PredictionArticleDataQueryDto> {
        return articleExposedRepository.findArticleListByTickerId(tickerId)
    }

    override fun getArticleList(
        userId: UUID?,
        page: Int,
        size: Int,
        tickerCodes: List<String>?,
        sentiment: String?,
        sort: String
    ): PageResponse<ArticleDataQueryDto> {
        val articleExposedList =
            articleExposedRepository.findAllArticleList(userId, tickerCodes, sentiment, page, size)
        return PageResponse(
            articleExposedList.content, page, size, articleExposedList.hasNext
        )
    }

    override fun getArticle(userId: UUID?, articleId: UUID): ArticleDetailQueryDto {
        return articleExposedRepository.findArticleDetailById(userId, articleId)
    }

}