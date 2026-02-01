package finn.response.kis

import java.math.BigDecimal

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
)