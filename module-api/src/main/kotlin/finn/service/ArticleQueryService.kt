package finn.service

import finn.entity.query.ArticleQ
import finn.paging.ArticlePageRequest
import finn.paging.PageResponse
import finn.policy.applyPageLimitPolicyForArticle
import finn.queryDto.ArticleDataQueryDto
import finn.queryDto.ArticleDetailQueryDto
import finn.repository.ArticleRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class ArticleQueryService(private val articleRepository: ArticleRepository) {

    fun getArticleDataForPredictionDetail(tickerId: UUID): List<ArticleDataQueryDto> {
        return articleRepository.getArticleDataForPredictionDetail(tickerId)
    }

    fun getArticleDataList(pageRequest: ArticlePageRequest): PageResponse<ArticleQ> {
        val pageResponse = articleRepository.getArticleList(
            pageRequest.page,
            pageRequest.size,
            pageRequest.tickerCode,
            pageRequest.sentiment,
            pageRequest.sort
        )

        return applyPageLimitPolicyForArticle(pageResponse)
    }

    fun getArticle(articleId: UUID): ArticleDetailQueryDto {
        return articleRepository.getArticle(articleId)
    }
}