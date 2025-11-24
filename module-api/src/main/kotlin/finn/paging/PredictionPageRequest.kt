package finn.paging

import io.swagger.v3.oas.annotations.media.Schema

data class PredictionPageRequest(
    @field:Schema(description = "페이지 번호", defaultValue = "0")
    override val page: Int = 0,
    @field:Schema(description = "페이지 당 데이터 개수", defaultValue = "5")
    override val size: Int = 5,
    @field:Schema(
        description = "정렬 기준",
        defaultValue = "popular",
        allowableValues = ["popular", "upward", "downward", "volatility"]
    )
    val sort: String,
    @field:Schema(
        description = "파라미터",
        allowableValues = ["keyword", "article", "graph"]
    )
    val param: String?
) : PageRequest
