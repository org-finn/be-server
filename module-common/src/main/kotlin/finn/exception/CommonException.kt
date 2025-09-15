package finn.exception

abstract class CommonException(
    message: String,
    cause: Throwable?
) : RuntimeException(message, cause) {

    abstract val code: ResponseCode

    constructor(message: String) : this(
        message, null
    )
}