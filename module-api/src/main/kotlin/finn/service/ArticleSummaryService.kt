package finn.service

import finn.entity.ArticleSummary
import finn.entity.ArticleSummaryAll
import finn.repository.ArticleSummaryRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class ArticleSummaryService(
    private val articleSummaryRepository: ArticleSummaryRepository
) {

    fun getArticleSummaryForAll(): ArticleSummaryAll {
        return articleSummaryRepository.findSummaryAll()
    }

    fun getArticleSummaryForTicker(tickerId: UUID): ArticleSummary {
        return articleSummaryRepository.findSummaryByTickerId(tickerId)
    }
}