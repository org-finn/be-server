package finn.config.kis

import finn.handler.KisRealTimeTickerPriceExecutionWebSocketHandler
import finn.repository.TickerRepository
import finn.service.KisAuthService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.client.WebSocketConnectionManager
import org.springframework.web.socket.client.standard.StandardWebSocketClient

@Configuration
class KisTickerPriceRealTimeClientConfig(
    private val kisAuthService: KisAuthService,
    private val kisProperties: KisProperties,
    private val kisRealTimeTickerPriceExecutionWebSocketHandler: KisRealTimeTickerPriceExecutionWebSocketHandler,
    private val tickerRepository: TickerRepository
) {
    private val log = KotlinLogging.logger {}
    private val KIS_WEBSOCKET_URL = kisProperties.wsBaseUrl + "/tryitout/HDFSCNT0"

    @Bean
    fun runKisConnection(): ApplicationRunner {
        return ApplicationRunner {
            try {
                // 1. 웹소켓 접속키 발급 (기존 로직 유지)
                val approvalKey = kisAuthService.getWebsocketApprovalKey()
                log.info { "Approval Key Acquired: $approvalKey" }

                // 2. 웹소켓 클라이언트 생성 및 URL 연결
                val client = StandardWebSocketClient()
                val manager = WebSocketConnectionManager(
                    client,
                    kisRealTimeTickerPriceExecutionWebSocketHandler, // 핸들러 등록
                    KIS_WEBSOCKET_URL            // 이미지의 정확한 주소 사용
                )

                manager.start()
                log.info { "Connecting to KIS WebSocket: $KIS_WEBSOCKET_URL" }

                // 3. 연결 후 잠시 대기했다가 구독 요청 (비동기라 연결 맺을 시간 필요)
                Thread.sleep(1000)

                val tickerCodeQueryDto = tickerRepository.findAllCode()
                tickerCodeQueryDto.tickerCodes.forEach {
                    kisRealTimeTickerPriceExecutionWebSocketHandler.sendSubscription(
                        approvalKey,
                        it.exchangeCode,
                        it.tickerCode
                    )
                }

            } catch (e: Exception) {
                log.error { "Failed to connect KIS WebSocket" + e.message }
            }
        }
    }
}