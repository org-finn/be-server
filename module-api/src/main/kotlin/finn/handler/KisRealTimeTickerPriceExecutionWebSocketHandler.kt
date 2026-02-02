package finn.handler

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import finn.converter.toDomainCode
import finn.converter.toKisCode
import finn.manager.TickerRealTimeCandleManager
import finn.repository.TickerRepository
import finn.response.kis.KisReaTimeTickerPriceResponse
import finn.service.KisAuthService
import finn.service.TickerPriceSseService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.math.BigDecimal

@Component
class KisRealTimeTickerPriceExecutionWebSocketHandler(
    private val sseService: TickerPriceSseService,
    private val candleManager: TickerRealTimeCandleManager,
    private val kisAuthService: KisAuthService,
    private val tickerRepository: TickerRepository
) : TextWebSocketHandler() {

    private val log = KotlinLogging.logger {}
    private val objectMapper = jacksonObjectMapper()
    private var session: WebSocketSession? = null
    private val TR_ID = "HDFSCNT0"


    // 연결 성공 시 '자동으로' 구독 요청 수행
    override fun afterConnectionEstablished(session: WebSocketSession) {
        log.info { "✅ KIS WebSocket Connected via Scheduler" }
        this.session = session

        // 키 발급 및 구독 요청 (별도 스레드나 안전하게 처리 권장)
        try {
            // 접속키는 유효기간이 있으므로 매번 새로 받거나 캐싱된 것 사용
            val approvalKey = kisAuthService.getWebsocketApprovalKey()
            val tickerCodeQueryDto = tickerRepository.findAllCode()
            tickerCodeQueryDto.tickerCodes.forEach {
                sendSubscription(approvalKey, it.exchangeCode, it.tickerCode)
                Thread.sleep(100) // KIS 서버 부하 방지용 텀
            }
        } catch (e: Exception) {
            log.error { "구독 요청 실패, ${e.message}" }
        }
    }

    /**
     * 구독 요청 실행, KIS 서버로 요청을 보냄
     */
    fun sendSubscription(approvalKey: String, marketCode: String, tickerCode: String) {
        val trKey = toKisCode(marketCode, tickerCode)

        val requestMap = mapOf(
            "header" to mapOf(
                "approval_key" to approvalKey,
                "custtype" to "P",
                "tr_type" to "1",
                "content-type" to "utf-8"
            ),
            "body" to mapOf(
                "input" to mapOf(
                    "tr_id" to TR_ID,
                    "tr_key" to trKey
                )
            )
        )

        val json = objectMapper.writeValueAsString(requestMap)
        session?.sendMessage(TextMessage(json))
        log.info { "Sent Subscription for $tickerCode ($trKey)" }
    }

    /**
     * 응답 받은 데이터 파싱 후 SSE, DB 데이터 전송
     */
    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val payload = message.payload

        // 데이터 포맷: 0(암호화X)|TR_ID|SEQ|Data...
        if (payload.startsWith("0") || payload.startsWith("1")) {
            val parts = payload.split("|")

            // parts[3]에 실제 데이터가 ^로 구분되어 들어옴
            if (parts.size > 3) {
                try {
                    val dto = parseExecutionData(parts[3])
                    if (dto != null) {
                        // 1. 실시간 차트용 SSE 전송
                        val tickerCode = toDomainCode(dto.symb)
                        val tickerId = tickerRepository.getTickerIdByTickerCode(tickerCode)
                        sseService.broadcast(dto, tickerId)

                        // 2. 1분봉 저장용 메모리 버퍼 갱신
                        candleManager.updatePrice(
                            tickerId = tickerId,
                            price = dto.last.toDouble(),
                            volume = dto.evol
                        )
                    }
                } catch (e: Exception) {
                    log.error("Parsing Error: ${e.message}")
                }
            }
        }
    }

    // 응답 명세서 순서대로 파싱
    private fun parseExecutionData(raw: String): KisReaTimeTickerPriceResponse? {
        val items = raw.split("^")
        // 데이터 필드 개수 체크 (최소 20개 이상이어야 함)
        if (items.size < 20) return null

        return KisReaTimeTickerPriceResponse(
            rsym = items[0],       // 실시간종목코드
            symb = items[1],       // 종목코드
            zdiv = items[2],       // 소수점자리수
            tymd = items[3],       // 현지영업일자
            xymd = items[4],       // 현지일자
            xhms = items[5],       // 현지시간
            kymd = items[6],       // 한국일자
            khms = items[7],       // 한국시간
            open = items[8].toBigDecimal(),
            high = items[9].toBigDecimal(),
            low = items[10].toBigDecimal(),
            last = items[11].toBigDecimal(), // 현재가
            sign = items[12],      // 대비구분
            diff = items[13].toBigDecimal(), // 전일대비
            rate = items[14].toBigDecimal(), // 등락율
            pbid = items[15].toBigDecimal(), // 매수호가
            pask = items[16].toBigDecimal(), // 매도호가
            vbid = items[17].toLong(),       // 매수잔량
            vask = items[18].toLong(),       // 매도잔량
            evol = items[19].toLong(),       // 체결량
            tvol = items[20].toLong(),       // 거래량
            tamt = if (items.size > 21) items[21].toBigDecimal() else BigDecimal.ZERO,
            mtyp = if (items.size > 24) items[24] else "" // 시장구분
        )
    }
}