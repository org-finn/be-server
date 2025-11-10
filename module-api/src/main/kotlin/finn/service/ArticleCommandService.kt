package finn.service

import finn.repository.ArticleRepository
import finn.repository.ArticleTickerRepository
import org.springframework.stereotype.Service

@Service
class ArticleCommandService(
    private val articleRepository: ArticleRepository,
    private val articleTickerRepository: ArticleTickerRepository
) {

}