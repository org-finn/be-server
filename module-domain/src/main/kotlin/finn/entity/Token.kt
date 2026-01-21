package finn.entity

class Token private constructor (
    val tokenValue: String,
) {

    companion object {
        fun create(tokenValue: String): Token {
            return Token(tokenValue)
        }
    }

}
