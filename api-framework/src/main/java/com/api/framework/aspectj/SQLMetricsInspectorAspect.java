package com.api.framework.aspectj;

import com.api.common.constant.CacheConstants;
import com.api.framework.interceptor.SQLDetailInspector;
import com.api.common.redis.RedisCache;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Aspect for tracking SQL execution performance and metrics.
 *
 * <p>Records: execution count, average time, max time, concurrency, success/fail count.
 */
@Slf4j
@Aspect
@Component
public class SQLMetricsInspectorAspect {

  @Resource private RedisCache redisCache;

  /** In-memory tracker for current concurrent executions per SQL method. */
  private final ConcurrentHashMap<String, AtomicInteger> concurrentMap = new ConcurrentHashMap<>();

  @Around("@annotation(com.api.framework.annotation.TrackSQLDetail)")
  public Object recordSQLMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
    log.info("✅ SQLMetricsInspectorAspect initialized successfully");

    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    com.api.framework.annotation.TrackSQLDetail annotation =
        signature.getMethod().getAnnotation(com.api.framework.annotation.TrackSQLDetail.class);

    // ✅ Build readable method name (repository method)
    String repositoryMethod =
        signature.getDeclaringType().getSimpleName() + "." + signature.getMethod().getName();

    String redisKey = CacheConstants.MONITOR_SQL_PREFIX + repositoryMethod;

    // Pass method name to Hibernate SQL inspector via ThreadLocal
    SQLDetailInspector.setCurrentMethod(repositoryMethod);

    AtomicInteger concurrent = concurrentMap.computeIfAbsent(redisKey, k -> new AtomicInteger(0));
    int concurrentNow = concurrent.incrementAndGet();

    long startTime = System.currentTimeMillis();
    boolean success = true;
    Object result;

    try {
      result = joinPoint.proceed();
      return result;
    } catch (Throwable ex) {
      success = false;
      throw ex;
    } finally {
      SQLDetailInspector.clear(); // clear ThreadLocal after
      long duration = System.currentTimeMillis() - startTime;
      concurrent.decrementAndGet();
      updateSQLStats(redisKey, repositoryMethod, duration, concurrentNow, success);
    }
  }

  private void updateSQLStats(
      String redisKey, String method, long duration, int concurrentNow, boolean success) {
    log.info(
        "[SQL-METRICS] Captured SQL metrics -> key={} duration={}ms success={}",
        redisKey,
        duration,
        success);
    try {
      Map<String, Object> metrics = redisCache.getCacheMap(redisKey);

      long executeCount = parseLong(metrics.get("ExecuteCount")) + 1;
      long totalTime = parseLong(metrics.get("TotalTime")) + duration;
      long successCount = parseLong(metrics.get("SuccessCount"));
      long failCount = parseLong(metrics.get("FailCount"));
      long maxTime = parseLong(metrics.get("TimeMillisMax"));
      int maxConcurrent = parseInt(metrics.get("ConcurrentMax"));

      if (success) successCount++;
      else failCount++;

      double avgTime = (double) totalTime / executeCount;
      if (duration > maxTime) maxTime = duration;
      if (concurrentNow > maxConcurrent) maxConcurrent = concurrentNow;

      redisCache.setCacheMapValue(redisKey, "SQLMethod", method);
      redisCache.setCacheMapValue(redisKey, "ExecuteCount", executeCount);
      redisCache.setCacheMapValue(redisKey, "TimeAverage", avgTime);
      redisCache.setCacheMapValue(redisKey, "TimeMillisMax", maxTime);
      redisCache.setCacheMapValue(redisKey, "SuccessCount", successCount);
      redisCache.setCacheMapValue(redisKey, "FailCount", failCount);
      redisCache.setCacheMapValue(redisKey, "ConcurrentMax", maxConcurrent);
      redisCache.setCacheMapValue(redisKey, "TotalTime", totalTime);

      log.debug(
          "[SQL-METRICS] key={} | method={} | duration={}ms | success={} | concurrentNow={}",
          redisKey,
          method,
          duration,
          success,
          concurrentNow);
    } catch (Exception e) {
      log.error("❌ Failed to update SQL metrics for key={}", redisKey, e);
    }
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
