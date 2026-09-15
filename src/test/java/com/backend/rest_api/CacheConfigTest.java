package com.backend.rest_api;

import com.backend.rest_api.config.CacheConfig;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class CacheConfigTest {

    private final CacheConfig cacheConfig = new CacheConfig();

    @Test
    void cacheManager_whenCreated_registersCommentsAndUsersCaches() {
        CacheManager cacheManager = cacheConfig.cacheManager();

        assertThat(cacheManager.getCache("comments")).isNotNull();
        assertThat(cacheManager.getCache("users")).isNotNull();
        assertThat(cacheManager.getCache("comments")).isInstanceOf(CaffeineCache.class);
    }

    @Configuration
    @EnableCaching
    static class TestConfig {

        @Bean
        CacheManager cacheManager() {
            return new CacheConfig().cacheManager();
        }

        @Bean
        CachedService cachedService() {
            return new CachedService();
        }
    }

    static class CachedService {
        private int invocations = 0;

        @Cacheable(cacheNames = "comments")
        public String getValue() {
            invocations++;
            return "value";
        }

        public int getInvocations() {
            return invocations;
        }
    }

    @Test
    void cacheableMethod_whenCalledTwice_invokesUnderlyingMethodOnce() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(TestConfig.class)) {
            CachedService cachedService = context.getBean(CachedService.class);

            String first = cachedService.getValue();
            String second = cachedService.getValue();

            assertThat(first).isEqualTo("value");
            assertThat(second).isEqualTo("value");
            assertThat(cachedService.getInvocations()).isEqualTo(1);
        }
    }
}