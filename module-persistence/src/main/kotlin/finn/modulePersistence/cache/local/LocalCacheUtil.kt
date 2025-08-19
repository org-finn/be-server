package finn.modulePersistence.cache.local

import com.github.benmanes.caffeine.cache.Cache
import finn.modulePersistence.cache.CacheUtil
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

@Component
class LocalCacheUtil(private val cache: Cache<String, CacheEntry<*>>) : CacheUtil {

    companion object {
        private val log = KotlinLogging.logger {}
    }

    override fun <T> get(key: String, classType: Class<T>): T? {
        val entry: CacheEntry<*>? = cache.getIfPresent(key)
        if (entry == null) {
            return null
        }

        if (entry.isExpired()) {
            cache.invalidate(key)
            return null
        }

        try {
            return classType.cast(entry.value)!!
        } catch (e: ClassCastException) {
            log.error(
                "[LocalCacheUtil]: Local Cache에서 값 가져오기 실패, key: {}, error: {}", key,
                e.message
            )
            return null
        }
    }

    override fun <T> set(key: String, value: T, ttl: Long) {
        cache.put(key, CacheEntry(value, ttl))
    }

    override fun deleteByPrefix(prefix: String) {
        val keysToDelete = this.cache.asMap().keys.stream()
            .filter { key: String? -> key!!.startsWith(prefix) }
            .toList()

        // 삭제할 키가 있는 경우에만 invalidateAll을 호출
        if (!keysToDelete.isEmpty()) {
            cache.invalidateAll(keysToDelete)
        }
    }
}