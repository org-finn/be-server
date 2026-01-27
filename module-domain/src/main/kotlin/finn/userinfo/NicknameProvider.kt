package finn.userinfo


import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import java.util.*

fun createUniqueNickname(): String {
    val alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray()
    return NanoIdUtils.randomNanoId(Random(), alphabet, 10)
}