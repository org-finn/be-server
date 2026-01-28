package finn.userinfo


import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import org.springframework.stereotype.Component
import java.util.*

@Component
class NicknameProvider {
    fun createUniqueNickname(): String {
        val alphabet =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray()
        return NanoIdUtils.randomNanoId(Random(), alphabet, 10)
    }
}