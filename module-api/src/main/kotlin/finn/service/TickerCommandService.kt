package finn.service

import finn.repository.TickerRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*

@Service
class TickerCommandService(
    private val tickerRepository: TickerRepository,
) {
    suspend fun updateAtr(tickerId: UUID, todayAtr: BigDecimal) {
        tickerRepository.updateTodayAtr(tickerId, todayAtr)
    }
}