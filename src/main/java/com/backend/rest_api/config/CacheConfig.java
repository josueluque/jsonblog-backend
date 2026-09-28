package com.backend.rest_api.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    private static final long DEFAULT_MAXIMUM_SIZE = 100;
    private static final long DEFAULT_EXPIRE_AFTER_WRITE_MINUTES = 10;

    private final long maximumSize;
    private final long expireAfterWriteMinutes;

    @Autowired
    public CacheConfig(
            @Value("${cache.maximum-size:100}") long maximumSize,
            @Value("${cache.expire-after-write-minutes:10}") long expireAfterWriteMinutes) {
        this.maximumSize = maximumSize;
        this.expireAfterWriteMinutes = expireAfterWriteMinutes;
    }

    public CacheConfig() {
        this(DEFAULT_MAXIMUM_SIZE, DEFAULT_EXPIRE_AFTER_WRITE_MINUTES);
    }

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("comments", "users");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterWrite(Duration.ofMinutes(expireAfterWriteMinutes)));
        return cacheManager;
    }

}
