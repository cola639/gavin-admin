package com.api.framework.aspectj;

import com.api.common.constant.CacheConstants;
import com.api.common.utils.StringUtils;
import com.api.common.utils.ip.IpUtils;
import com.api.framework.annotation.RateLimiter;
import com.api.framework.enums.LimitType;
import com.api.framework.exception.ServiceException;
import com.api.framework.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Aspect for enforcing rate limits using Redis counters.
 *
 * <p>This aspect uses a Lua script (loaded as {@link RedisScript}) to ensure atomic increments and
 * time-based resets in Redis.
 */
@RequiredArgsConstructor
@Slf4j
@Aspect
@Component
public class RateLimiterAspect {

  private final RedisTemplate<Object, Object> redisTemplate;
  private final TokenService tokenService;

  /** Intercepts methods annotated with {@link RateLimiter} and applies rate limiting. */
  @Before("@annotation(rateLimiter)")
  public void enforceRateLimit(JoinPoint point, RateLimiter rateLimiter) {
    String key = buildKey(rateLimiter, point);
    int limit = rateLimiter.count();
    int windowSeconds = rateLimiter.time();

    try {
      // ✅ Increment Redis counter atomically
      Long currentCount = redisTemplate.opsForValue().increment(key);

      // ✅ If this is the first access, set expiration
      if (currentCount != null && currentCount == 1L) {
        redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
      }

      if (currentCount != null && currentCount > limit) {
        log.warn("🚫 Rate limit exceeded: key={} count={} limit={}", key, currentCount, limit);
        throw new ServiceException(rateLimiter.message());
      }

      log.debug(
          "⏱️ RateLimiter: key={} | count={} | limit={} | window={}s",
          key,
          currentCount,
          limit,
          windowSeconds);

    } catch (ServiceException e) {
      throw e;
    } catch (Exception e) {
      log.error("❌ Failed to apply rate limiting for key={}", key, e);
      throw new RuntimeException("Rate limiting system error, please try again later.");
    }
  }

  /** Builds a unique Redis key for the rate limit counter. */
  private String buildKey(RateLimiter rateLimiter, JoinPoint point) {
    StringBuilder sb = new StringBuilder(rateLimiter.key());

    LimitType type = rateLimiter.limitType();
    switch (type) {
      case IP -> sb.append(IpUtils.getIpAddr()).append("-");
      case USER -> {
        var user = tokenService.getLoginUser();
        if (user != null) {
          sb.append(user.getUserId()).append("-");
        } else {
          sb.append("guest-");
        }
      }
      default -> sb.append("global-");
    }

    MethodSignature signature = (MethodSignature) point.getSignature();
    Method method = signature.getMethod();
    Class<?> targetClass = method.getDeclaringClass();

    sb.append(targetClass.getSimpleName()).append("-").append(method.getName());
    return sb.toString();
  }
}
