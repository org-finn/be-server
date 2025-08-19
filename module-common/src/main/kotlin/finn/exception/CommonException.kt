package finn.exception

abstract class CommonException(
    message: String,
    cause: Throwable?
) : RuntimeException(message, cause) {

    constructor(message: String) : this(
        message, null
    )
}