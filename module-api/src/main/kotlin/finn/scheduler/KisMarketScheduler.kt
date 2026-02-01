package com.example.stock.scheduler

import finn.entity.query.MarketStatus
import finn.repository.MarketStatusRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.socket.client.WebSocketConnectionManager
import java.time.Clock
import java.time.LocalDate

@Component
class KisMarketScheduler(
    private val connectionManager: WebSocketConnectionManager,
    private val clock: Clock,
    private val marketStatusRepository: MarketStatusRepository
) {
    private val log = KotlinLogging.logger {}

    // 30분마다 실행 (미국 장 시간 체크)
    @Scheduled(cron = "30 * * * * *")
    fun checkMarketHours() {
        val marketStatus =
            marketStatusRepository.getOptionalMarketStatus(LocalDate.now(clock))
        if (MarketStatus.checkIsOpened(marketStatus, clock)) {
            if (!connectionManager.isRunning) {
                log.info { "📢 미국 장 시작 (Market Open) - 웹소켓 연결 시도" }
                connectionManager.start()
            }
        } else {
            if (connectionManager.isRunning) {
                log.info { "💤 미국 장 종료 (Market Closed) - 웹소켓 연결 종료" }
                connectionManager.stop()
            }
        }
    }
}