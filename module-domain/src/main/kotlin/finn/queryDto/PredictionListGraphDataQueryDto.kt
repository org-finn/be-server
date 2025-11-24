package finn.queryDto

import java.math.BigDecimal

interface PredictionListGraphDataQueryDto {

    fun marketOpen(): Boolean

    fun priceData(): List<BigDecimal>
}