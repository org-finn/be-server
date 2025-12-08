package finn.repository.impl

import finn.entity.ArticleSummaryAll
import finn.mapper.toDomain
import finn.repository.ArticleSummaryRepository
import finn.repository.exposed.ArticleSummaryExposedRepository
import org.springframework.stereotype.Repository

@Repository
class ArticleSummaryRepositoryImpl(
    private val articleSummaryExposedRepository: ArticleSummaryExposedRepository
) : ArticleSummaryRepository {

    override fun findSummaryAll(): ArticleSummaryAll {
        return toDomain(articleSummaryExposedRepository.findSummaryAll())
    }
}