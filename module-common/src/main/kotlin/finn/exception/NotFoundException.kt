package finn.exception

open class NotFoundException(
    message: String,
    cause: Throwable?
) : CommonException(message, cause) {
    override val code: ResponseCode = ResponseCode.NOT_FOUND

    constructor(message: String) : this(
        message, null
    )

}