package finn.paging

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern
import java.util.*

data class ArticlePageRequest(
    @field:Schema(description = "페이지 번호", defaultValue = "0")
    override val page: Int = 0,
    @field:Schema(description = "페이지 당 데이터 개수", defaultValue = "10")
    override val size: Int = 10,
    @field:Schema(
        description = "종목 필터링 기준",
    )
    val tickerId: UUID? = null,
    @field:Schema(
        description = "감정 필터링 기준",
        allowableValues = ["positive", "negative"]
    )
    @field:Pattern(
        regexp = "^(positive|negative)$",
        message = "sentiment 값은 ''positive', 'negative' 중 하나여야 합니다."
    )
    val sentiment: String? = null,
    @field:Schema(
        description = "정렬 기준",
        defaultValue = "recent",
        allowableValues = ["recent"]
    )
    @field:Pattern(
        regexp = "^(recent)$",
        message = "sort 값은 recent만 허용합니다."
    )
    val sort: String
) : PageRequest
