package finn.queryDto

import java.math.BigDecimal

data class PredictionListGraphDataQueryDto(
    val marketOpen: Boolean,
    val priceData: List<BigDecimal>
)