package finn.service

import finn.entity.Article
import finn.repository.ArticleRepository
import org.springframework.stereotype.Service

@Service
class ArticleCommandService(
    private val articleRepository: ArticleRepository
) {

    fun saveArticleList(articleList: List<Article>) {

    }

}