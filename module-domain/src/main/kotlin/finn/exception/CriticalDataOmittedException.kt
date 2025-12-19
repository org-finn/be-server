package finn.exception

class CriticalDataOmittedException(
    message: String,
    cause: Throwable?
) : BadRequestException(message, cause) {

    constructor(message: String) : this(
        message, null
    )
}