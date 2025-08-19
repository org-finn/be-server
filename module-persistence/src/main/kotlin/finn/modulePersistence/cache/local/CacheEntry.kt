package finn.modulePersistence.cache.local

class CacheEntry<T>(
    val value: T,
    val expireTime: Long
) {

    fun isExpired(): Boolean {
        return System.currentTimeMillis() > expireTime
    }
}