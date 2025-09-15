package finn.apiSpec

import finn.paging.PredictionPageRequest
import finn.response.ErrorResponse
import finn.response.SuccessResponse
import finn.response.prediciton.PredictionDetailResponse
import finn.response.prediciton.PredictionListResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*


@Tag(name = "예측 API", description = "예측 관련 API")
@RequestMapping("/api/v1/prediction")
@ResponseStatus(HttpStatus.OK)
interface PredictionApiSpec {
    @Operation(
        summary = "정렬된 종목 예측 리스트 조회", description = "정렬 옵션과 페이징을 적용하여 종목 예측 리스트를 조회합니다."
    )
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200",
            description = "종목 예측 목록을 성공적으로 조회하였습니다."
        ), ApiResponse(
            responseCode = "400",
            description = "유효하지 않은 조회 조건입니다.",
            content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
        )]
    )
    @GetMapping("/ticker")
    fun getTickerPredictionList(
        @ParameterObject pageRequest: PredictionPageRequest
    ): SuccessResponse<PredictionListResponse>

    @Operation(summary = "종목 예측 상세 조회", description = "특정 종목의 예측 상세 정보를 조회합니다.")
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200",
            description = "종목 예측 상세 정보를 성공적으로 조회하였습니다."
        ), ApiResponse(
            responseCode = "404",
            description = "존재하지 않는 종목 Id 값입니다.",
            content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
        )]
    )
    @GetMapping("/ticker/{tickerId}")
    fun getTickerPredictionDetail(
        @Parameter(
            description = "종목 ID (UUID)",
            required = true,
            example = "a1b2c3d4-e5f6-7890-1234-567890abcdef"
        ) @PathVariable tickerId: UUID
    ): SuccessResponse<PredictionDetailResponse>

}