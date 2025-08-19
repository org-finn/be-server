package finn.modulePersistence.cache

interface CacheUtil {
    fun <T> get(key: String, classType: Class<T>): T?

    fun <T> set(key: String, value: T, ttl: Long)

    fun deleteByPrefix(prefix: String)
}