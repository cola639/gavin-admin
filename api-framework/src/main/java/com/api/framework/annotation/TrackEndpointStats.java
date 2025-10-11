package com.api.framework.annotation;

import java.lang.annotation.*;

/**
 * Annotation for tracking endpoint performance metrics.
 *
 * <p>When applied to a controller method, an aspect measures: - Request duration - Success /
 * failure count - Concurrent invocations - Average and max response time
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TrackEndpointStats {

  /** Optional custom key for Redis storage. Defaults to request URI. */
  String value() default "";
}
