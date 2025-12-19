package finn.repository.exposed

import finn.entity.ExchangeRateExposed
import finn.exception.NotFoundDataException
import finn.table.ExchangeRateTable
import org.jetbrains.exposed.sql.SortOrder
import org.springframework.stereotype.Repository

@Repository
class ExchangeRateExposedRepository {

    fun findByIndexCode(indexCode: String): ExchangeRateExposed {
        return ExchangeRateExposed
            .find {
                ExchangeRateTable.indexCode eq indexCode
            }.orderBy(ExchangeRateTable.date to SortOrder.DESC)
            .limit(1)
            .firstOrNull()
            ?: throw NotFoundDataException("index code로 조회한 환율 데이터가 존재하지 않습니다. code를 확인해주세요.")
    }
}