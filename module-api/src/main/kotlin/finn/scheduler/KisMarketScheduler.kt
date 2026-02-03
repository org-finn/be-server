package com.example.stock.scheduler

import finn.entity.query.MarketStatus
import finn.repository.MarketStatusRepository
import finn.transaction.ExposedTransactional
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.socket.client.WebSocketConnectionManager
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime

@Component
class KisMarketScheduler(
    private val connectionManager: WebSocketConnectionManager,
    private val clock: Clock,
    private val marketStatusRepository: MarketStatusRepository
) {
    private val log = KotlinLogging.logger {}

    // 10분마다 실행 (미국 장 시간 체크)
    @Scheduled(cron = "0 0/10 * * * *")
    @ExposedTransactional(readOnly = true)
    fun checkMarketHours() {

        log.info { "⏰ 스케줄러 실행 중... (Time: ${LocalDateTime.now(clock)})" }

        val marketStatus =
            marketStatusRepository.getOptionalMarketStatus(LocalDate.now(clock))

        // 오픈 상태인지 체크
        val isOpen = MarketStatus.checkIsOpened(marketStatus, clock)

        if (isOpen) {
            if (!connectionManager.isRunning) {
                log.info { "📢 미국 장 시작 (Market Open) - 웹소켓 연결 시도" }
                connectionManager.start()
            } else {
                // 이미 연결되어 있어서 아무것도 안 함
                log.debug { "이미 연결되어 있음 (Market Open)" }
            }
        } else {
            if (connectionManager.isRunning) {
                log.info { "💤 미국 장 종료 (Market Closed) - 웹소켓 연결 종료" }
                connectionManager.stop()
            } else {
                // 이미 종료되어 있어서 아무것도 안 함
                log.debug { "이미 종료되어 있음 (Market Closed)" }
            }
        }
    }
}