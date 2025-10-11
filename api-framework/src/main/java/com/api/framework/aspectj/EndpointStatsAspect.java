package com.api.framework.aspectj;

import com.api.common.redis.RedisCache;
import com.api.framework.annotation.TrackEndpointStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.api.common.constant.CacheConstants.MONITOR_URI_KEY;

/**
 * Aspect for measuring API endpoint metrics such as execution time, success/failure rate, and
 * concurrency. Results are aggregated in Redis.
 *
 * <p>Metrics stored per endpoint:
 *
 * <ul>
 *   <li>RequestCount
 *   <li>RequestTimeAverage
 *   <li>RequestTimeMillisMax
 *   <li>ConcurrentMax
 *   <li>SuccessCount
 *   <li>FailCount
 * </ul>
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class EndpointStatsAspect {

  private final RedisCache redisCache;

  /** Tracks live concurrent requests per endpoint (in-memory) */
  private final ConcurrentHashMap<String, AtomicInteger> concurrentMap = new ConcurrentHashMap<>();

  @Around("@annotation(com.api.framework.annotation.TrackEndpointStats)")
  public Object measureExecution(ProceedingJoinPoint joinPoint) throws Throwable {
    long startTime = System.currentTimeMillis();

    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    TrackEndpointStats annotation = signature.getMethod().getAnnotation(TrackEndpointStats.class);
    String uri =
        annotation.value().isEmpty() ? signature.getMethod().getName() : annotation.value();

    // Track concurrent requests safely
    AtomicInteger current = concurrentMap.computeIfAbsent(uri, k -> new AtomicInteger(0));
    int concurrentNow = current.incrementAndGet();

    Object result;
    boolean success = true;

    try {
      result = joinPoint.proceed();
    } catch (Throwable ex) {
      success = false;
      throw ex;
    } finally {
      long duration = System.currentTimeMillis() - startTime;
      current.decrementAndGet();
      updateStats(uri, duration, concurrentNow, success);
    }

    return result;
  }

  /**
   * Updates Redis metrics for a given endpoint.
   *
   * @param uri endpoint key
   * @param duration execution time in ms
   * @param concurrentNow current concurrent count
   * @param success whether the request succeeded
   */
  private void updateStats(String uri, long duration, int concurrentNow, boolean success) {
    String redisKey = MONITOR_URI_KEY + uri;

    try {
      // ✅ Increment counters
      increment(redisKey, "RequestCount", 1);
      increment(redisKey, success ? "SuccessCount" : "FailCount", 1);
      increment(redisKey, "RequestTimeTotal", duration);

      // ✅ Compute and store average
      Map<String, Object> stats = redisCache.getCacheMap(redisKey);
      long count = parseLong(stats.get("RequestCount"));
      long total = parseLong(stats.get("RequestTimeTotal"));
      if (count > 0) {
        double avg = (double) total / count;
        redisCache.setCacheMapValue(redisKey, "RequestTimeAverage", String.format("%.2f", avg));
      }

      // ✅ Update max duration
      long currentMax = parseLong(stats.get("RequestTimeMillisMax"));
      if (duration > currentMax) {
        redisCache.setCacheMapValue(redisKey, "RequestTimeMillisMax", duration);
      }

      // ✅ Update max concurrency
      int concurrentMax = parseInt(stats.get("ConcurrentMax"));
      if (concurrentNow > concurrentMax) {
        redisCache.setCacheMapValue(redisKey, "ConcurrentMax", concurrentNow);
      }

      // ✅ Set TTL (e.g., 24h)
      redisCache.expire(redisKey, Duration.ofHours(24).toSeconds());

      // ✅ Log metrics
      log.debug(
          "[Metrics] URI={} | Time={}ms | Success={} | ConcurrentNow={}",
          uri,
          duration,
          success,
          concurrentNow);

    } catch (Exception e) {
      log.error("❌ Failed to update metrics for URI: {}", uri, e);
    }
  }

  private void increment(String key, String field, long value) {
    Map<String, Object> map = redisCache.getCacheMap(key);
    long current = parseLong(map.get(field));
    redisCache.setCacheMapValue(key, field, current + value);
  }

  private long parseLong(Object value) {
    if (value == null) return 0L;
    try {
      return Long.parseLong(value.toString());
    } catch (NumberFormatException e) {
      return 0L;
    }
  }

  private int parseInt(Object value) {
    if (value == null) return 0;
    try {
      return Integer.parseInt(value.toString());
    } catch (NumberFormatException e) {
      return 0;
    }
  }
}
