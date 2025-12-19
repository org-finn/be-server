package finn.exception

open class ServerErrorException(
    message: String,
    cause: Throwable?
) : CommonException(message, cause) {
    override val code: ResponseCode = ResponseCode.SERVER_ERROR

    constructor(message: String) : this(
        message, null
    )

}