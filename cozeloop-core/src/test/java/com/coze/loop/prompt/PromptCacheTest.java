package com.coze.loop.prompt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.coze.loop.entity.Prompt;

/** Unit tests for PromptCache. */
class PromptCacheTest {

  private PromptCache cache;
  private Function<String, Prompt> loader;

  @BeforeEach
  void setUp() {
    loader =
        key -> {
          Prompt prompt = new Prompt();
          prompt.setPromptKey(key);
          return prompt;
        };

    PromptCache.PromptCacheConfig config =
        PromptCache.PromptCacheConfig.builder()
            .maxSize(100)
            .expireAfterWriteMinutes(60)
            .refreshAfterWriteMinutes(30)
            .build();

    cache = new PromptCache(config, loader);
  }

  @Test
  void testGetSync() {
    Prompt prompt = cache.getSync("test-key");

    assertThat(prompt).isNotNull();
    assertThat(prompt.getPromptKey()).isEqualTo("test-key");
  }

  @Test
  void testGetAsync() throws Exception {
    CompletableFuture<Prompt> future = cache.get("test-key");
    Prompt prompt = future.get();

    assertThat(prompt).isNotNull();
    assertThat(prompt.getPromptKey()).isEqualTo("test-key");
  }

  @Test
  void testPut() {
    Prompt prompt = new Prompt();
    prompt.setPromptKey("custom-key");

    cache.put("custom-key", prompt);
    Prompt cached = cache.getSync("custom-key");

    assertThat(cached).isNotNull();
    assertThat(cached.getPromptKey()).isEqualTo("custom-key");
  }

  @Test
  void testInvalidate() {
    cache.getSync("test-key");
    cache.invalidate("test-key");

    // After invalidation, should reload from loader
    Prompt prompt = cache.getSync("test-key");
    assertThat(prompt).isNotNull();
  }

  @Test
  void testInvalidateAll() {
    cache.getSync("key1");
    cache.getSync("key2");

    cache.invalidateAll();

    // Cache should be empty, but getSync will reload
    Prompt prompt = cache.getSync("key1");
    assertThat(prompt).isNotNull();
  }

  @Test
  void testStats() {
    cache.getSync("test-key");

    assertThat(cache.stats()).isNotNull();
  }

  @Test
  void testCacheReuse() {
    Prompt prompt1 = cache.getSync("test-key");
    Prompt prompt2 = cache.getSync("test-key");

    // Should return same instance from cache
    assertThat(prompt1).isSameAs(prompt2);
  }
}
