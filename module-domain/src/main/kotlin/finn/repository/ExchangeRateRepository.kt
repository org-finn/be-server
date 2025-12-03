package finn.repository

import finn.entity.ExchangeRate

interface ExchangeRateRepository {

    fun findByIndexInfo(indexCode: String) : ExchangeRate
}