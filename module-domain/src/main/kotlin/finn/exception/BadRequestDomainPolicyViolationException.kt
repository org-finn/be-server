package finn.exception

class BadRequestDomainPolicyViolationException(
    message: String,
    cause: Throwable?
) : CommonException(message, cause) {
    constructor(message: String) : this(
        message, null
    )
}