package finn.repository.impl

import finn.repository.ExponentRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class ExponentRepositoryImpl(

) : ExponentRepository {
    override suspend fun getRecentExponentByCode(
        code: String,
        priceDate: LocalDateTime
    ): Double {
        TODO("Not yet implemented")
    }
}