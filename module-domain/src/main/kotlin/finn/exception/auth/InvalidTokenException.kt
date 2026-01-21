package finn.exception.auth

import finn.exception.UnAuthorizedException

/**
 * 만료되었거나 유효하지 않은 토큰
 */
class InvalidTokenException(
    message: String,
    cause: Throwable?
) : UnAuthorizedException(message, cause) {

    constructor(message: String) : this(
        message, null
    )
}