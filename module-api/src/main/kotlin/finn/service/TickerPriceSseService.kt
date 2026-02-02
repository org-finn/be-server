package finn.service

import finn.response.kis.KisReaTimeTickerPriceResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

@Service
class TickerPriceSseService {

    private val log = KotlinLogging.logger {}

    // Key: 종목코드, Value: 구독자 리스트
    // CopyOnWriteArrayList: 읽기(broadcast) 시 락을 걸지 않아 성능이 좋고 안전함
    private val emitterMap = ConcurrentHashMap<String, CopyOnWriteArrayList<SseEmitter>>()

    private val TIMEOUT = 30 * 60 * 1000L // 30분

    /**
     * 1. 클라이언트 구독 (Controller에서 호출)
     */
    fun subscribe(stockCode: String): SseEmitter {
        val emitter = SseEmitter(TIMEOUT)

        // 해당 종목 리스트가 없으면 생성
        emitterMap.computeIfAbsent(stockCode) { CopyOnWriteArrayList() }.add(emitter)

        // 콜백 설정: 연결 종료/타임아웃 시 리스트에서 제거
        emitter.onCompletion { removeEmitter(stockCode, emitter) }
        emitter.onTimeout { removeEmitter(stockCode, emitter) }
        emitter.onError { removeEmitter(stockCode, emitter) }

        // 첫 연결 시 더미 데이터 전송 (503 에러 방지)
        sendToClient(emitter, "connect", "connected to $stockCode")

        log.info { "Client subscribed to $stockCode. Total: ${emitterMap[stockCode]?.size}" }
        return emitter
    }

    /**
     * 2. 실시간 데이터 브로드캐스팅 (WebSocket Handler에서 호출)
     */
    fun broadcast(dto: KisReaTimeTickerPriceResponse) {
        val emitters = emitterMap[dto.symb] // 종목코드로 구독자 찾기

        emitters?.forEach { emitter ->
            try {
                emitter.send(
                    SseEmitter.event()
                        .name("ticker-price")
                        .data(dto)
                ) // DTO는 자동으로 JSON 변환됨
            } catch (e: IOException) {
                // 전송 실패한 클라이언트(연결 끊김 등)는 제거
                removeEmitter(dto.symb, emitter)
            }
        }
    }

    private fun sendToClient(emitter: SseEmitter, name: String, data: Any) {
        try {
            emitter.send(SseEmitter.event().name(name).data(data))
        } catch (e: IOException) {
            // 무시 (어차피 연결 끊기면 콜백에서 처리됨)
        }
    }

    private fun removeEmitter(stockCode: String, emitter: SseEmitter) {
        emitterMap[stockCode]?.remove(emitter)
    }
}