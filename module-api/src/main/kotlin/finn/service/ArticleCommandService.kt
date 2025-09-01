package finn.service

import finn.entity.command.ArticleC
import finn.entity.command.ArticleInsight
import finn.repository.ArticleRepository
import finn.repository.ArticleTickerRepository
import org.springframework.stereotype.Service

@Service
class ArticleCommandService(
    private val articleRepository: ArticleRepository,
    private val articleTickerRepository: ArticleTickerRepository
) {

    fun saveArticleList(article: ArticleC, insights: List<ArticleInsight>) {
        val articleId = articleRepository.saveArticle(article, insights)
        if (articleId != null && insights.isNotEmpty()) {
            articleTickerRepository.saveArticleTicker(articleId, article.title, insights)
        }
    }

}