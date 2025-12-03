package finn.repository.impl

import finn.entity.ExchangeRate
import finn.mapper.toDomain
import finn.repository.ExchangeRateRepository
import finn.repository.exposed.ExchangeRateExposedRepository
import org.springframework.stereotype.Repository

@Repository
class ExchangeRateRepositoryImpl(
    private val exchangeRateExposedRepository: ExchangeRateExposedRepository
) : ExchangeRateRepository {

    override fun findByIndexInfo(indexCode: String): ExchangeRate {
        return toDomain(exchangeRateExposedRepository.findByIndexCode(indexCode))
    }
}