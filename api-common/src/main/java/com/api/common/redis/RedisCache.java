package com.api.common.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings({"unchecked", "rawtypes"})
public class RedisCache {

  private final RedisTemplate<Object, Object> redisTemplate;
  private final ObjectMapper objectMapper;

  /** Store any object. String will be stored as raw string; others stored as JSON. */
  public <T> void setCacheObject(final String key, final T value) {
    if (key == null || key.isBlank()) {
      return;
    }
    if (value == null) {
      deleteObject(key);
      return;
    }
    try {
      if (value instanceof String str) {
        redisTemplate.opsForValue().set(key, str);
        return;
      }
      String json = objectMapper.writeValueAsString(value);
      redisTemplate.opsForValue().set(key, json);
    } catch (Exception e) {
      log.error("Failed to write Redis key={}", key, e);
    }
  }

  /**
   * Store any object with expiration. String will be stored as raw string; others stored as JSON.
   */
  public <T> void setCacheObject(
      final String key, final T value, final Integer timeout, final TimeUnit unit) {
    if (key == null || key.isBlank()) {
      return;
    }
    if (value == null) {
      deleteObject(key);
      return;
    }
    try {
      if (value instanceof String str) {
        redisTemplate.opsForValue().set(key, str, timeout, unit);
        return;
      }
      String json = objectMapper.writeValueAsString(value);
      redisTemplate.opsForValue().set(key, json, timeout, unit);
    } catch (Exception e) {
      log.error("Failed to write Redis key={} with TTL", key, e);
    }
  }

  public boolean expire(final String key, final long timeout) {
    return expire(key, timeout, TimeUnit.SECONDS);
  }

  public boolean expire(final String key, final long timeout, final TimeUnit unit) {
    Boolean ok = redisTemplate.expire(key, timeout, unit);
    return Boolean.TRUE.equals(ok);
  }

  public long getExpire(final String key) {
    Long ttl = redisTemplate.getExpire(key);
    return ttl == null ? -1L : ttl;
  }

  public Boolean hasKey(final String key) {
    return redisTemplate.hasKey(key);
  }

  /**
   * Raw get: returns the stored value as-is. - For JSON stored entries: returns JSON string - For
   * String entries: returns raw string
   */
  public <T> T getCacheObject(final String key) {
    ValueOperations<String, T> operation = (ValueOperations) redisTemplate.opsForValue();
    return operation.get(key);
  }

  /** Typed get: reads raw String or JSON String and converts to clazz. */
  public <T> T getCacheObject(final String key, Class<T> clazz) {
    try {
      ValueOperations<Object, Object> ops = redisTemplate.opsForValue();
      Object value = ops.get(key);
      if (value == null) {
        return null;
      }

      if (clazz.isInstance(value)) {
        return clazz.cast(value);
      }

      // If it was stored as raw string and caller expects String
      if (clazz == String.class) {
        return clazz.cast(value.toString());
      }

      // Otherwise treat it as JSON
      return objectMapper.readValue(value.toString(), clazz);
    } catch (Exception e) {
      log.error("Failed to deserialize Redis key={} to {}", key, clazz.getSimpleName(), e);
      return null;
    }
  }

  public boolean deleteObject(final String key) {
    return Boolean.TRUE.equals(redisTemplate.delete(key));
  }

  /** Delete multiple keys. Returns true if at least one key was deleted. */
  public boolean deleteObject(Collection<String> keys) {
    if (keys == null || keys.isEmpty()) {
      return false;
    }
    Long deleted =
        redisTemplate.delete(
            new ArrayList<>(keys)); // returns count :contentReference[oaicite:2]{index=2}
    long count = deleted == null ? 0L : deleted;
    log.debug("Deleted {} keys from Redis", count);
    return count > 0;
  }

  public <T> void setCacheMap(final String key, final Map<String, T> dataMap) {
    if (dataMap != null) {
      redisTemplate.opsForHash().putAll(key, dataMap);
    }
  }

  public <T> Map<String, T> getCacheMap(final String key) {
    Map<Object, Object> rawMap = redisTemplate.opsForHash().entries(key);
    Map<String, T> result = new HashMap<>();
    rawMap.forEach((mapKey, value) -> result.put(String.valueOf(mapKey), (T) value));
    return result;
  }

  public <T> void setCacheMapValue(final String key, final String hKey, final T value) {
    redisTemplate.opsForHash().put(key, hKey, value);
  }

  public <T> T getCacheMapValue(final String key, final String hKey) {
    HashOperations<String, String, T> opsForHash = (HashOperations) redisTemplate.opsForHash();
    return opsForHash.get(key, hKey);
  }

  public boolean deleteCacheMapValue(final String key, final String hKey) {
    return redisTemplate.opsForHash().delete(key, hKey) > 0;
  }

  public Set<String> keys(final String pattern) {
    Set<Object> rawKeys = redisTemplate.keys(pattern);
    if (rawKeys == null) {
      return Collections.emptySet();
    }
    return rawKeys.stream().map(Object::toString).collect(Collectors.toSet());
  }
}
