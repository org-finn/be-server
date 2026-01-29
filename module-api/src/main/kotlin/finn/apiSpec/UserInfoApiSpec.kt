package finn.apiSpec

import finn.request.userinfo.FavoriteTickerRequest
import finn.request.userinfo.NicknameRequest
import finn.resolver.UserId
import finn.response.SuccessResponse
import finn.response.userinfo.FavoriteTickerResponse
import finn.response.userinfo.NicknameValidationResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import java.util.*

@Tag(name = "유저 API", description = "유저 관련 API")
@RequestMapping("/api/v1/my")
interface UserInfoApiSpec {
    @Operation(
        summary = "닉네임 중복 검사", description = "변경하려는 닉네임의 중복검사를 수행합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "닉네임 중복 검사 결과 조회 성공"
            ),
            ApiResponse(
                responseCode = "400",
                description = "유효하지 않은 조건의 닉네임입니다. (영대소문자/한글/숫자 조합으로 최대 12자)"
            ),
            ApiResponse(
                responseCode = "401",
                description = "유효하지 않은 토큰 등의 이유로 인증에 실패하였습니다."
            ),
        ]
    )
    @PostMapping("/nickname/validation")
    fun checkNicknameValidation(
        @RequestParam("nickname", required = true) nickname: String
    ): SuccessResponse<NicknameValidationResponse>


    @Operation(
        summary = "닉네임 수정", description = "닉네임을 수정합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "닉네임 수정 성공"
            ),
            ApiResponse(
                responseCode = "400",
                description = "유효하지 않은 조건의 닉네임입니다. (영대소문자/한글/숫자 조합으로 최대 12자)"
            ),
            ApiResponse(
                responseCode = "400",
                description = "이미 존재하는 닉네임입니다."
            ),
            ApiResponse(
                responseCode = "401",
                description = "유효하지 않은 토큰 등의 이유로 인증에 실패하였습니다."
            ),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 회원입니다."
            )
        ]
    )
    @PutMapping("/nickname")
    fun updateNickname(
        @RequestBody nicknameRequest: NicknameRequest,
        @UserId userId: UUID
    ): SuccessResponse<Nothing>

    @Operation(
        summary = "관심 종목 리스트 조회", description = "유저의 관심 종목들을 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "관심 종목 리스트 조회 성공"
            ),
            ApiResponse(
                responseCode = "401",
                description = "유효하지 않은 토큰 등의 이유로 인증에 실패하였습니다."
            )
        ]
    )
    @GetMapping("/favorite/tickers")
    fun getFavoriteTickers(
        @UserId userId: UUID
    ): SuccessResponse<FavoriteTickerResponse>


    @Operation(
        summary = "관심 종목 수정", description = "유저의 관심 종목을 수정합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "관심 종목 리스트 수정 성공"
            ),
            ApiResponse(
                responseCode = "400",
                description = "중복 혹은 유효하지 않은 종목 값으로 인해 수정에 실패했습니다."
            ),
            ApiResponse(
                responseCode = "401",
                description = "유효하지 않은 토큰 등의 이유로 인증에 실패하였습니다."
            )
        ]
    )
    @PutMapping("/favorite/tickers")
    fun updateFavoriteTickers(
        @RequestBody favoriteTickerRequest: FavoriteTickerRequest,
        @UserId userId: UUID
    ): SuccessResponse<Nothing>

    @Operation(
        summary = "관심 종목 등록/해제", description = "유저의 단일 관심 종목을 등록/해제합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "관심 종목 등록/해제 성공"
            ),
            ApiResponse(
                responseCode = "400",
                description = "유효하지 않은 변경 상태 모드 값입니다."
            ),
            ApiResponse(
                responseCode = "400",
                description = "유효하지 않은 종목 값으로 인해 수정에 실패했습니다."
            ),
            ApiResponse(
                responseCode = "401",
                description = "유효하지 않은 토큰 등의 이유로 인증에 실패하였습니다."
            )
        ]
    )
    @PutMapping("/favorite/ticker")
    fun updateFavoriteSingleTicker(
        @RequestParam("tickerCode", required = true) tickerCode: String,
        @RequestParam("mode", required = true, defaultValue = "on") mode: String,
        @UserId userId: UUID
    ): SuccessResponse<Nothing>

    @Operation(
        summary = "회원 탈퇴", description = "회원 탈퇴를 수행합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "회원 탈퇴 성공"
            ),
            ApiResponse(
                responseCode = "401",
                description = "유효하지 않은 토큰 등의 이유로 인증에 실패하였습니다."
            ),
        ]
    )
    @DeleteMapping("/withdrawn")
    fun withdrawn(@UserId userId: UUID): SuccessResponse<Nothing>

}
