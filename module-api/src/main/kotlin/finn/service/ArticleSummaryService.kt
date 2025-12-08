package finn.service

import finn.entity.ArticleSummaryAll
import finn.repository.ArticleSummaryRepository
import org.springframework.stereotype.Service

@Service
class ArticleSummaryService(
    private val articleSummaryRepository: ArticleSummaryRepository
) {

    fun getArticleSummaryForAll(): ArticleSummaryAll {
        return articleSummaryRepository.findSummaryAll()
    }
}