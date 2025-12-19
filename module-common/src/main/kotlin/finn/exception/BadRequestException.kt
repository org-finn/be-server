package finn.exception

open class BadRequestException(
    message: String,
    cause: Throwable?
) : CommonException(message, cause) {
    override val code: ResponseCode = ResponseCode.BAD_REQUEST

    constructor(message: String) : this(
        message, null
    )

}