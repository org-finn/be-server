package finn.service

import finn.paging.ArticlePageRequest
import finn.paging.PageResponse
import finn.policy.applyPageLimitPolicyForArticle
import finn.queryDto.ArticleDataQueryDto
import finn.queryDto.ArticleDetailQueryDto
import finn.queryDto.PredictionArticleDataQueryDto
import finn.repository.ArticleRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class ArticleQueryService(private val articleRepository: ArticleRepository) {

    fun getArticleDataForPredictionDetail(tickerId: UUID): List<PredictionArticleDataQueryDto> {
        return articleRepository.getArticleDataForPredictionDetail(tickerId)
    }

    fun getArticleDataList(
        userId: UUID?,
        pageRequest: ArticlePageRequest
    ): PageResponse<ArticleDataQueryDto> {
        val pageResponse = articleRepository.getArticleList(
            userId,
            pageRequest.page,
            pageRequest.size,
            pageRequest.tickerCode,
            pageRequest.sentiment,
            pageRequest.sort
        )

        return applyPageLimitPolicyForArticle(pageResponse)
    }

    fun getArticle(userId: UUID?, articleId: UUID): ArticleDetailQueryDto {
        return articleRepository.getArticle(userId, articleId)
    }

    fun searchArticles(keyword: String) : List<ArticleDataQueryDto> {
        return articleRepository.findArticleListByKeyword(keyword)
    }
}