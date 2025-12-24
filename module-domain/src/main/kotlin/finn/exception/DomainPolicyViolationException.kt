package finn.exception

class DomainPolicyViolationException(
    message: String,
    cause: Throwable?
) : BadRequestException(message, cause) {

    constructor(message: String) : this(
        message, null
    )
}