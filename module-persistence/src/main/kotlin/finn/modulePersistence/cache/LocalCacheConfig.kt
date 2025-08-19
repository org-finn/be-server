package finn.modulePersistence.cache

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import finn.modulePersistence.cache.local.CacheEntry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class LocalCacheConfig {
    @Bean
    fun caffeineCache(): Cache<String, CacheEntry<*>> {
        return Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(1, TimeUnit.DAYS)
            .recordStats()
            .build<String, CacheEntry<*>>()
    }
}