package finn.mapper

import finn.queryDto.FavoriteArticleQueryDto
import finn.response.article.ArticleListResponse.ArticleDataResponse
import finn.response.userinfo.FavoriteArticleResponse
import org.springframework.stereotype.Component

@Component
class FavoriteArticleDtoMapper {

    companion object {
        fun toDto(queryDtoList: List<FavoriteArticleQueryDto>): FavoriteArticleResponse {
            return FavoriteArticleResponse(
                queryDtoList.map {
                    ArticleDataResponse(
                        it.articleId,
                        it.title,
                        it.description,
                        it.shortCompanyNames,
                        it.thumbnailUrl,
                        it.contentUrl,
                        it.publishedDate.toString(),
                        it.source,
                        true
                    )
                }.toList()
            )
        }
    }
}