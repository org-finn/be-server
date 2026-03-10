package finn.response.kis

import finn.response.graph.TickerRealTimeStreamResponse
import java.math.BigDecimal
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class KisReaTimeTickerPriceResponse(
    val rsym: String,      // 0. RSYM: 실시간종목코드 (예: DNASAAPL)
    val symb: String,      // 1. SYMB: 종목코드 (예: AAPL)
    val zdiv: String,      // 2. ZDIV: 소수점자리수
    val tymd: String,      // 3. TYMD: 현지영업일자
    val xymd: String,      // 4. XYMD: 현지일자
    val xhms: String,      // 5. XHMS: 현지시간
    val kymd: String,      // 6. KYMD: 한국일자
    val khms: String,      // 7. KHMS: 한국시간
    val open: BigDecimal,  // 8. OPEN: 시가
    val high: BigDecimal,  // 9. HIGH: 고가
    val low: BigDecimal,   // 10. LOW: 저가
    val last: BigDecimal,  // 11. LAST: 현재가 (중요!)
    val sign: String,      // 12. SIGN: 대비구분
    val diff: BigDecimal,  // 13. DIFF: 전일대비
    val rate: BigDecimal,  // 14. RATE: 등락율
    val pbid: BigDecimal,  // 15. PBID: 매수호가
    val pask: BigDecimal,  // 16. PASK: 매도호가
    val vbid: Long,        // 17. VBID: 매수잔량
    val vask: Long,        // 18. VASK: 매도잔량
    val evol: Long,        // 19. EVOL: 체결량 (이 틱에서 거래된 양 - 중요!)
    val tvol: Long,        // 20. TVOL: 거래량 (누적)
    val tamt: BigDecimal,  // 21. TAMT: 거래대금
    val mtyp: String       // 24. MTYP: 시장구분 (중간 건너뜀 주의)
) {
    // EST -> KST 시간 파싱 필요
    fun toStreamResponse(): TickerRealTimeStreamResponse {
        val NY_ZONE = ZoneId.of("America/New_York")
        val KST_ZONE = ZoneId.of("Asia/Seoul")

        val formattedTime = try {
            // 1. 현지 시간(미국 동부) 파싱 (예: "093000" -> 09:30:00)
            val localTime = LocalTime.parse(this.xhms, DateTimeFormatter.ofPattern("HHmmss"))

            // 2. 미국 동부 기준의 '오늘 날짜' 획득 (서머타임 계산을 위해 날짜가 반드시 필요함)
            val todayNY = ZonedDateTime.now(NY_ZONE).toLocalDate()

            // 3. 날짜 + 시간 + 타임존을 결합하여 미국 동부 ZonedDateTime 객체 생성
            val zonedDateTimeNY = ZonedDateTime.of(todayNY, localTime, NY_ZONE)

            // 4. KST 타임존으로 변환 후 "HH:mm:ss" 형태로 포맷팅
            zonedDateTimeNY.withZoneSameInstant(KST_ZONE)
                .format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        } catch (e: Exception) {
            // 파싱 실패 시 원본 데이터(xhms)를 그대로 반환하여 에러 방지
            this.xhms
        }

        return TickerRealTimeStreamResponse(
            time = formattedTime,
            open = this.open,
            high = this.high,
            low = this.low,
            close = this.last,
            volume = this.evol
        )
    }
}