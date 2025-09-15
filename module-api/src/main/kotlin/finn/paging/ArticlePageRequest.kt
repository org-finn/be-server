package finn.paging

import io.swagger.v3.oas.annotations.media.Schema

data class ArticlePageRequest (
    @field:Schema(description = "페이지 번호", defaultValue = "0")
    override val page: Int = 0,
    @field:Schema(description = "페이지 당 데이터 개수", defaultValue = "10")
    override val size: Int = 10,
    @field:Schema(
        description = "필터링 기준",
        defaultValue = "all",
//        allowableValues = ["all", "positive", "negative"]
        allowableValues = ["all"]
        )
    val filter: String,
    @field:Schema(
        description = "정렬 기준",
        defaultValue = "recent",
        allowableValues = ["recent"]
    )
    val sort: String
): PageRequest
