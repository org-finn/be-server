package finn.policy

import finn.paging.PageResponse
import finn.queryDto.ArticleDataQueryDto


fun applyPageLimitPolicyForArticle(pageResponse: PageResponse<ArticleDataQueryDto>): PageResponse<ArticleDataQueryDto> {
    val finalHasNext = if (pageResponse.page < 9) {
        pageResponse.hasNext
    } else {
        false
    }
    return pageResponse.copy(hasNext = finalHasNext)
}