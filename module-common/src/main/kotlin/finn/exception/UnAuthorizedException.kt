package finn.exception

open class UnAuthorizedException(
    message: String,
    cause: Throwable?
) : CommonException(message, cause) {
    override val code: ResponseCode = ResponseCode.UNAUTHORIZED

    constructor(message: String) : this(
        message, null
    )

}