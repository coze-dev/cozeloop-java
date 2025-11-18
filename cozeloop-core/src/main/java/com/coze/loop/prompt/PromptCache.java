package com.coze.loop.prompt;

import com.coze.loop.entity.Prompt;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Cache for prompts using Caffeine.
 * Supports LRU eviction and automatic refresh.
 */
public class PromptCache {
    private static final Logger logger = LoggerFactory.getLogger(PromptCache.class);
    
    private final AsyncLoadingCache<String, Prompt> cache;
    
    /**
     * Create a PromptCache with custom configuration.
     *
     * @param config cache configuration
     * @param loader function to load prompt when not in cache
     */
    public PromptCache(PromptCacheConfig config, Function<String, Prompt> loader) {
        this.cache = Caffeine.newBuilder()
            .maximumSize(config.getMaxSize())
            .expireAfterWrite(config.getExpireAfterWriteMinutes(), TimeUnit.MINUTES)
            .refreshAfterWrite(config.getRefreshAfterWriteMinutes(), TimeUnit.MINUTES)
            .recordStats()
            .buildAsync((key, executor) -> CompletableFuture.supplyAsync(() -> {
                logger.debug("Loading prompt from source: {}", key);
                return loader.apply(key);
            }, executor));
    }
    
    /**
     * Get prompt from cache, loading if necessary.
     *
     * @param key the cache key
     * @return CompletableFuture of prompt
     */
    public CompletableFuture<Prompt> get(String key) {
        return cache.get(key);
    }
    
    /**
     * Get prompt from cache synchronously.
     *
     * @param key the cache key
     * @return prompt or null if not found
     */
    public Prompt getSync(String key) {
        try {
            return cache.get(key).join();
        } catch (Exception e) {
            logger.error("Error getting prompt from cache: {}", key, e);
            return null;
        }
    }
    
    /**
     * Put prompt into cache.
     *
     * @param key the cache key
     * @param prompt the prompt
     */
    public void put(String key, Prompt prompt) {
        cache.put(key, CompletableFuture.completedFuture(prompt));
    }
    
    /**
     * Invalidate a cache entry.
     *
     * @param key the cache key
     */
    public void invalidate(String key) {
        cache.synchronous().invalidate(key);
    }
    
    /**
     * Invalidate all cache entries.
     */
    public void invalidateAll() {
        cache.synchronous().invalidateAll();
    }
    
    /**
     * Get cache statistics.
     *
     * @return cache stats
     */
    public com.github.benmanes.caffeine.cache.stats.CacheStats stats() {
        return cache.synchronous().stats();
    }
    
    /**
     * Configuration for prompt cache.
     */
    public static class PromptCacheConfig {
        private long maxSize = 1000;
        private long expireAfterWriteMinutes = 60;
        private long refreshAfterWriteMinutes = 30;
        
        public long getMaxSize() {
            return maxSize;
        }
        
        public void setMaxSize(long maxSize) {
            this.maxSize = maxSize;
        }
        
        public long getExpireAfterWriteMinutes() {
            return expireAfterWriteMinutes;
        }
        
        public void setExpireAfterWriteMinutes(long expireAfterWriteMinutes) {
            this.expireAfterWriteMinutes = expireAfterWriteMinutes;
        }
        
        public long getRefreshAfterWriteMinutes() {
            return refreshAfterWriteMinutes;
        }
        
        public void setRefreshAfterWriteMinutes(long refreshAfterWriteMinutes) {
            this.refreshAfterWriteMinutes = refreshAfterWriteMinutes;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private final PromptCacheConfig config = new PromptCacheConfig();
            
            public Builder maxSize(long size) {
                config.maxSize = size;
                return this;
            }
            
            public Builder expireAfterWriteMinutes(long minutes) {
                config.expireAfterWriteMinutes = minutes;
                return this;
            }
            
            public Builder refreshAfterWriteMinutes(long minutes) {
                config.refreshAfterWriteMinutes = minutes;
                return this;
            }
            
            public PromptCacheConfig build() {
                return config;
            }
        }
    }
}

