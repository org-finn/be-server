package finn.repository

import finn.entity.query.MarketStatus
import java.time.LocalDate

interface MarketStatusRepository {

    fun getOptionalMarketStatus(today: LocalDate): MarketStatus?
}