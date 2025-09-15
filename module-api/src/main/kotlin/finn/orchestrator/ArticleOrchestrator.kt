package finn.orchestrator

import finn.mapper.toDto
import finn.paging.ArticlePageRequest
import finn.response.article.ArticleListResponse
import finn.service.ArticleQueryService
import finn.transaction.ExposedTransactional
import org.springframework.stereotype.Service

@Service
@ExposedTransactional(readOnly = true)
class ArticleOrchestrator(
    private val articleQueryService: ArticleQueryService
) {

    fun getRecentArticleList(pageRequest: ArticlePageRequest): ArticleListResponse {
        val articleList = articleQueryService.getArticleDataList(pageRequest)
        return toDto(articleList)
    }
}