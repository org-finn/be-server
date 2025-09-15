package finn.policy

import finn.entity.query.ArticleQ
import finn.paging.PageResponse


fun applyPageLimitPolicyForArticle(pageResponse: PageResponse<ArticleQ>): PageResponse<ArticleQ> {
    val finalHasNext = if (pageResponse.page < 9) {
        pageResponse.hasNext
    } else {
        false
    }
    return pageResponse.copy(hasNext = finalHasNext)
}