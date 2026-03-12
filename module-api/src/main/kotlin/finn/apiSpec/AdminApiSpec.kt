package finn.apiSpec

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping

@Tag(name = "관리자 API", description = "스케줄러 수동 제어 등 관리자 전용 API")
@RequestMapping("/api/admin/scheduler") // 기존에 경로가 없었다면 추가하는 것을 권장합니다.
interface AdminApiSpec {

    @Operation(
        summary = "웹소켓 연결 상태 수동 체크",
        description = "스케줄러를 기다리지 않고 KIS 웹소켓 연결 상태 점검 및 연결을 즉시 강제 트리거합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "수동 트리거 성공"),
            ApiResponse(responseCode = "400", description = "인증 헤더 누락 또는 오류")
        ]
    )
    @PostMapping("/trigger/market-hours")
    fun triggerMarketHours(
        @Parameter(description = "관리자 인증용 시크릿 키", required = true)
        @RequestHeader("X-Admin-Secret", required = true) secret: String
    ): ResponseEntity<String>

    @Operation(
        summary = "1분봉 DB 저장 수동 트리거",
        description = "스케줄러를 기다리지 않고 메모리에 있는 1분봉 데이터를 즉시 DynamoDB에 적재합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "수동 트리거 성공"),
            ApiResponse(responseCode = "400", description = "인증 헤더 누락 또는 오류")
        ]
    )
    @PostMapping("/trigger/flush-candles")
    fun triggerFlushCandles(
        @Parameter(description = "관리자 인증용 시크릿 키", required = true)
        @RequestHeader("X-Admin-Secret", required = true) secret: String
    ): ResponseEntity<String>
}