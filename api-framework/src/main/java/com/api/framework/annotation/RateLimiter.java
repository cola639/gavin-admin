package com.api.framework.annotation;

import com.api.common.constant.CacheConstants;
import com.api.framework.enums.LimitType;

import java.lang.annotation.*;

/**
 * Annotation for API rate limiting.
 *
 * <p>Usage example:
 *
 * <pre>
 * @RateLimiter(time = 60, count = 10, limitType = LimitType.IP)
 * public AjaxResult getUserInfo() {
 *     return AjaxResult.success("OK");
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {

  /** Base key for rate-limiting counter in Redis. */
  String key() default CacheConstants.RATE_LIMIT_KEY;

  /** Time window in seconds for rate limiting. */
  int time() default 60;

  /** Maximum number of allowed requests during the time window. */
  int count() default 100;

  /** Strategy for identifying the requester (e.g., IP, USER_ID, DEFAULT). */
  LimitType limitType() default LimitType.DEFAULT;

  /** Custom message returned when limit exceeded. */
  String message() default "Too many requests, please try again later.";
}
