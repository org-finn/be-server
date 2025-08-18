package finn.moduleDomain.repository

import finn.moduleDomain.entity.MarketStatus
import java.time.LocalDate

interface MarketStatusRepository {

    fun getOptionalMarketStatus(today: LocalDate): MarketStatus?
}