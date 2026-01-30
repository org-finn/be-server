package finn.mapper

import finn.queryDto.FavoriteArticleQueryDto
import finn.response.userinfo.FavoriteArticleResponse
import finn.response.userinfo.FavoriteArticleResponse.FavoriteArticle
import org.springframework.stereotype.Component

@Component
class FavoriteArticleDtoMapper {

    companion object {
        fun toDto(queryDtoList: List<FavoriteArticleQueryDto>): FavoriteArticleResponse {
            return FavoriteArticleResponse(
                queryDtoList.map {
                    FavoriteArticle(
                        it.articleId,
                        it.title,
                        it.thumbnailUrl
                    )
                }.toList()
            )
        }
    }
}