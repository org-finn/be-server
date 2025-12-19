package finn.exception

class SQSException(
    message: String,
    cause: Throwable?
) : ServerErrorException(message, cause) {

    constructor(message: String) : this(
        message, null
    )
}