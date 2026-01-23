package finn.exception.auth

import finn.exception.UnAuthorizedException

/**
 * 토큰 탈취 리스크 예외 -> 쿠키 삭제 유도
 */
class TokenStolenRiskException(
    message: String,
    cause: Throwable?
) : UnAuthorizedException(message, cause) {

    constructor(message: String) : this(
        message, null
    )
}