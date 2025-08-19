package finn.cache

import finn.cache.local.LocalCacheUtil
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Primary
@Component
class HybridCacheUtil(private val localCacheUtil: LocalCacheUtil) : CacheUtil {

    override fun <T> get(key: String, classType: Class<T>): T? {
        return localCacheUtil.get(key, classType)
    }

    override fun <T> set(key: String, value: T, ttl: Long) {
        localCacheUtil.set(key, value, ttl)
    }

    override fun deleteByPrefix(prefix: String) {
        localCacheUtil.deleteByPrefix(prefix)
    }
}