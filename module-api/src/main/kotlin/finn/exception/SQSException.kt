package finn.exception

class SQSException(
    message: String,
    cause: Throwable?
) : CommonException(message, cause) {
    override val code: ResponseCode = ResponseCode.SERVER_ERROR

    constructor(message: String) : this(
        message, null
    )
}