package finn.orchestrator

import finn.mapper.ArticleSummaryDtoMapper.Companion.toDto
import finn.response.articleSummary.ArticleSummaryAllResponse
import finn.service.ArticleSummaryService
import finn.transaction.ExposedTransactional
import org.springframework.stereotype.Service

@Service
@ExposedTransactional(readOnly = true)
class ArticleSummaryOrchestrator(
    private val articleSummaryService: ArticleSummaryService
) {

    fun getArticleSummaryForAll(): ArticleSummaryAllResponse {
        val articleSummaryAll = articleSummaryService.getArticleSummaryForAll()
        return toDto(articleSummaryAll)
    }
}