package finn.repository

import finn.entity.MarketStatus
import java.time.LocalDate

interface MarketStatusRepository {

    fun getOptionalMarketStatus(today: LocalDate): MarketStatus?
}