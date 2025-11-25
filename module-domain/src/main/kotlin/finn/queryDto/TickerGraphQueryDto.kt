package finn.queryDto

import java.math.BigDecimal
import java.time.LocalDate

data class TickerGraphQueryDto(
    val date: LocalDate,
    val price: BigDecimal,
    val changeRate: BigDecimal,
    val positiveArticleCount: Long,
    val negativeArticleCount: Long
)