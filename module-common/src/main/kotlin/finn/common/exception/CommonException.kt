package finn.common.exception

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