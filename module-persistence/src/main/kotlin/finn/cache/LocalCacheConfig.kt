package finn.cache

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import finn.cache.local.CacheEntry
import org.springframework.cache.caffeine.CaffeineCacheManager
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

    @Bean
    fun cacheManager(): CaffeineCacheManager {
        val cacheManager = CaffeineCacheManager()

        cacheManager.registerCustomCache(
            "tickerSearchList",
            Caffeine.newBuilder()
                .expireAfterWrite(6, TimeUnit.HOURS)
                .maximumSize(100)
                .build()
        )
        return cacheManager
    }
}