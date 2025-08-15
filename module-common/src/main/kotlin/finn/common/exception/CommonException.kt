package finn.common.exception

import finn.common.apiResponse.ResponseCode

abstract class CommonException(
    val responseCode: ResponseCode,
    message: String?,
    cause: Throwable?
) : RuntimeException(message, cause) {

    constructor(responseCode: ResponseCode, message: String) : this(
        responseCode, message, null
    )

    constructor(responseCode: ResponseCode) : this(
        responseCode, responseCode.defaultMessage, null
    )
}