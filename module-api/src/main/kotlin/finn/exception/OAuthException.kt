package finn.exception

class OAuthException(
    message: String,
    cause: Throwable?
) : ServerErrorException(message, cause) {

    constructor(message: String) : this(
        message, null
    )
}