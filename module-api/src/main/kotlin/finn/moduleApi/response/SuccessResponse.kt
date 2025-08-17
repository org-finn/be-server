package finn.moduleApi.response

class SuccessResponse<T> (val code: String, message: String, val data: T?) {
    constructor(code: String, message: String) : this(code, message, null)
}