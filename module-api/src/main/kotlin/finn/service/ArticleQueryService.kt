package finn.service

import finn.entity.Article
import finn.paging.ArticlePageRequest
import finn.paging.PageResponse
import finn.queryDto.ArticleDataQueryDto
import finn.repository.ArticleRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class ArticleQueryService(private val articleRepository: ArticleRepository) {

    fun getArticleDataForPredictionDetail(tickerId: UUID): List<ArticleDataQueryDto> {
        return articleRepository.getArticleDataForPredictionDetail(tickerId)
    }

    fun getArticleDataList(pageRequest: ArticlePageRequest): PageResponse<Article> {
        return articleRepository.getArticleList(
            pageRequest.page,
            pageRequest.size,
            pageRequest.filter,
            pageRequest.sort
        )
    }
}