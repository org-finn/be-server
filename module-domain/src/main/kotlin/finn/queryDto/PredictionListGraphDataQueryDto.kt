package finn.queryDto

import java.math.BigDecimal

interface PredictionListGraphDataQueryDto {

    fun isMarketOpen(): Boolean

    fun priceData(): List<BigDecimal>
}