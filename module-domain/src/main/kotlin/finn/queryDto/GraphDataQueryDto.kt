package finn.queryDto

import java.math.BigDecimal

data class GraphDataQueryDto(
    val marketOpen: Boolean,
    val priceData: List<BigDecimal>
)