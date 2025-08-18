package finn.moduleApi.service

import finn.moduleDomain.converter.BusinessDayLocalizer
import finn.moduleDomain.entity.MarketStatus
import finn.moduleDomain.repository.MarketStatusRepository
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDate

@Service
class MarketStatusQueryService(
    private val marketStatusRepository: MarketStatusRepository,
    private val clock: Clock
) {

    fun getTodayMarketStatus(): MarketStatus {
        val today = LocalDate.now(clock)

        // 1. 주말인 경우
        if (MarketStatus.isWeekend(today)) {
            return MarketStatus.getWeekendMarketStatus(today)
        }

        // 2. 영속성 계층에서 데이터 조회(휴장일/부분 휴장일), 없으면 완전 개장일로 간주하고 반환
        val marketStatus = marketStatusRepository.getOptionalMarketStatus(today)
        if (marketStatus != null) {
            return marketStatus
        }
        return MarketStatus.getFullOpenedMarketStatus(today, BusinessDayLocalizer)
    }

}