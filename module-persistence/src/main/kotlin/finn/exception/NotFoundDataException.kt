package finn.exception

class NotFoundDataException(message: String, cause: Throwable?) :
    CommonException(message, cause) {
    override val code: ResponseCode = ResponseCode.NOT_FOUND

    constructor(message: String) : this(message, null)
}