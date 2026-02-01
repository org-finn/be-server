package finn.config.kis

import finn.handler.KisRealTimeTickerPriceExecutionWebSocketHandler
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.client.WebSocketConnectionManager
import org.springframework.web.socket.client.standard.StandardWebSocketClient

@Configuration
class KisTickerPriceRealTimeClientConfig(
    private val kisProperties: KisProperties,
    private val kisRealTimeTickerPriceExecutionWebSocketHandler: KisRealTimeTickerPriceExecutionWebSocketHandler,
) {
    private val log = KotlinLogging.logger {}
    private val KIS_WEBSOCKET_ENDPOINT = "/tryitout/HDFSCNT0"

    @Bean
    fun webSocketConnectionManager(): WebSocketConnectionManager {
        val url = kisProperties.wsBaseUrl + KIS_WEBSOCKET_ENDPOINT
        val client = StandardWebSocketClient()
        val manager = WebSocketConnectionManager(
            client,
            kisRealTimeTickerPriceExecutionWebSocketHandler,
            url
        )
        // manager.start()는 여기서 호출하지 않음 (스케줄러가 함)
        return manager
    }
}