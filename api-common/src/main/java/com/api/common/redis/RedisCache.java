package com.api.common.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Utility class for common Redis operations.
 *
 * <p>Supports caching and retrieving: - Basic objects (String, Integer, custom objects, etc.) -
 * Lists, Sets, Maps, and Hash structures - Expiration and key management
 */
@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings({"unchecked", "rawtypes"})
public class RedisCache {

  private final RedisTemplate<Object, Object> redisTemplate;

  /** Cache a basic object (String, Integer, entity, etc.) */
  public <T> void setCacheObject(final String key, final T value) {
    redisTemplate.opsForValue().set(key, value);
  }

  /** Cache a basic object with expiration. */
  public <T> void setCacheObject(
      final String key, final T value, final Integer timeout, final TimeUnit timeUnit) {
    redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
  }

  /** Set expiration time (in seconds by default). */
  public boolean expire(final String key, final long timeout) {
    return expire(key, timeout, TimeUnit.SECONDS);
  }

  /** Set expiration time with a custom unit. */
  public boolean expire(final String key, final long timeout, final TimeUnit unit) {
    return redisTemplate.expire(key, timeout, unit);
  }

  /** Get remaining expiration time of a key. */
  public long getExpire(final String key) {
    return redisTemplate.getExpire(key);
  }

  /** Check if a key exists. */
  public Boolean hasKey(final String key) {
    return redisTemplate.hasKey(key);
  }

  /** Retrieve a cached basic object. */
  public <T> T getCacheObject(final String key) {
    ValueOperations<String, T> operation = (ValueOperations) redisTemplate.opsForValue();
    return operation.get(key);
  }

  /** Delete a single key. */
  public boolean deleteObject(final String key) {
    return Boolean.TRUE.equals(redisTemplate.delete(key));
  }

  /** Delete multiple keys. */
  public boolean deleteObject(final Collection<String> keys) {
    Long deletedCount = redisTemplate.delete(Collections.singleton(keys));
    return deletedCount != null && deletedCount > 0;
  }

  /** Cache a list of objects. */
  public <T> long setCacheList(final String key, final List<T> dataList) {
    Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
    return count == null ? 0 : count;
  }

  /** Retrieve a cached list. */
  public <T> List<T> getCacheList(final String key) {
    return (List<T>) redisTemplate.opsForList().range(key, 0, -1);
  }

  /** Cache a set of objects. */
  public <T> BoundSetOperations<String, T> setCacheSet(final String key, final Set<T> dataSet) {
    BoundSetOperations<String, T> setOperation =
        (BoundSetOperations) redisTemplate.boundSetOps(key);
    if (dataSet != null) {
      dataSet.forEach(setOperation::add);
    }
    return setOperation;
  }

  /** Retrieve a cached set. */
  public <T> Set<T> getCacheSet(final String key) {
    return (Set<T>) redisTemplate.opsForSet().members(key);
  }

  /** Cache a map. */
  public <T> void setCacheMap(final String key, final Map<String, T> dataMap) {
    if (dataMap != null) {
      redisTemplate.opsForHash().putAll(key, dataMap);
    }
  }

  /** Retrieve a cached map with type safety. */
  public <T> Map<String, T> getCacheMap(final String key) {
    Map<Object, Object> rawMap = redisTemplate.opsForHash().entries(key);
    Map<String, T> result = new HashMap<>();
    rawMap.forEach((mapKey, value) -> result.put(String.valueOf(mapKey), (T) value));
    return result;
  }

  /** Put a value into a hash. */
  public <T> void setCacheMapValue(final String key, final String hKey, final T value) {
    redisTemplate.opsForHash().put(key, hKey, value);
  }

  /** Retrieve a value from a hash. */
  public <T> T getCacheMapValue(final String key, final String hKey) {
    HashOperations<String, String, T> opsForHash = (HashOperations) redisTemplate.opsForHash();
    return opsForHash.get(key, hKey);
  }

  /** Retrieve multiple values from a hash. */
  public <T> List<T> getMultiCacheMapValue(final String key, final Collection<Object> hKeys) {
    return (List<T>) redisTemplate.opsForHash().multiGet(key, hKeys);
  }

  /** Delete a value from a hash. */
  public boolean deleteCacheMapValue(final String key, final String hKey) {
    return redisTemplate.opsForHash().delete(key, hKey) > 0;
  }

  /** Get all keys matching a pattern. */
  public Set<String> keys(final String pattern) {
    Set<Object> rawKeys = redisTemplate.keys(pattern);
    if (rawKeys == null) {
      return Collections.emptySet();
    }
    return rawKeys.stream().map(Object::toString).collect(Collectors.toSet());
  }
}
