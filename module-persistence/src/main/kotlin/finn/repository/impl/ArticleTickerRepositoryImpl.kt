package finn.repository.impl

import finn.repository.ArticleTickerRepository
import finn.repository.exposed.ArticleTickerExposedRepository
import finn.repository.exposed.TickerExposedRepository
import org.springframework.stereotype.Repository

@Repository
class ArticleTickerRepositoryImpl(
    private val articleTickerExposedRepository: ArticleTickerExposedRepository,
    private val tickerExposedRepository: TickerExposedRepository
) : ArticleTickerRepository {

}