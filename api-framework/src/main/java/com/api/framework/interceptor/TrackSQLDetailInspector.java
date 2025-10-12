package com.api.framework.interceptor;

import com.api.common.constant.CacheConstants;
import com.api.common.redis.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.springframework.context.ApplicationContext;

/**
 * Hibernate StatementInspector to capture SQL details. Not a Spring bean — Hibernate instantiates
 * it directly.
 */
@Slf4j
public class TrackSQLDetailInspector implements StatementInspector {

  private static ApplicationContext context;

  /** Provide Spring context manually so we can fetch RedisCache */
  public static void setApplicationContext(ApplicationContext ctx) {
    context = ctx;
  }

  private static final ThreadLocal<String> currentMethodKey = new ThreadLocal<>();

  public static void setCurrentMethod(String methodName) {
    currentMethodKey.set(methodName);
  }

  public static void clear() {
    currentMethodKey.remove();
  }

  @Override
  public String inspect(String sql) {
    String methodName = currentMethodKey.get();
    if (methodName == null) return sql;

    try {
      RedisCache redisCache = context.getBean(RedisCache.class);
      String redisKey = CacheConstants.MONITOR_SQL_PREFIX + methodName;
      redisCache.setCacheMapValue(redisKey, "SQLDetail", sql);
      log.debug("[SQL-DETAIL] method={} | sql={}", methodName, sql);
    } catch (Exception e) {
      log.error("❌ Failed to record SQL detail", e);
    }

    return sql;
  }
}
