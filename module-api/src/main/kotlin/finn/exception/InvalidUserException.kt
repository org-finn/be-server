package finn.exception

class InvalidUserException(
    message: String,
    cause: Throwable?
) : UnAuthorizedException(message, cause) {

    constructor(message: String) : this(
        message, null
    )
}