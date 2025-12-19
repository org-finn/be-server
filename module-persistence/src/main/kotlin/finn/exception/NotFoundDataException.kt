package finn.exception

class NotFoundDataException(message: String, cause: Throwable?) :
    NotFoundException(message, cause) {

    constructor(message: String) : this(message, null)
}