package finn.exception

class NotSupportedTypeException(
    message: String,
    cause: Throwable?
) : BadRequestException(message, cause) {

    constructor(message: String) : this(
        message, null
    )
}