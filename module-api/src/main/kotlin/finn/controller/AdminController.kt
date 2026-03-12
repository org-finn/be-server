package finn.controller

import finn.exception.UnAuthorizedException
import finn.scheduler.KisMarketScheduler
import finn.scheduler.TickerRealTimePricePersistenceScheduler
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class AdminController(
    private val marketScheduler: KisMarketScheduler,
    private val persistenceScheduler: TickerRealTimePricePersistenceScheduler,
    @Value("\${admin.secret-key}") private val adminSecretKey: String,
) {

    // 1. 웹소켓 연결 상태 수동 체크 (checkMarketHours)
    @PostMapping("/trigger/market-hours")
    fun triggerMarketHours(
        secret: String
    ): ResponseEntity<String> {
        if (secret != adminSecretKey) {
            throw UnAuthorizedException("admin 인증 실패")
        }
        marketScheduler.checkMarketHours()
        return ResponseEntity.ok("✅ [수동 실행] KIS 웹소켓 연결 상태 체크가 트리거되었습니다.")
    }

    // 2. 1분봉 DB 저장 수동 체크 (flushCandlesToDb)
    @PostMapping("/trigger/flush-candles")
    fun triggerFlushCandles(
        secret: String
    ): ResponseEntity<String> {
        if (secret != adminSecretKey) {
            throw UnAuthorizedException("admin 인증 실패")
        }
        persistenceScheduler.flushCandlesToDb()
        return ResponseEntity.ok("✅ [수동 실행] 1분봉 DynamoDB 저장이 트리거되었습니다.")
    }
}