package com.api.framework.interceptor;

import com.api.common.constant.CacheConstants;
import com.api.common.redis.RedisCache;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.springframework.stereotype.Component;

/** Intercepts SQL executed by Hibernate and links it to repository methods tracked by Aspect. */
@Slf4j
@Component
public class SQLDetailInspector implements StatementInspector {

  @Resource private RedisCache redisCache;

  private static final ThreadLocal<String> currentMethodKey = new ThreadLocal<>();

  public static void setCurrentMethod(String methodName) {
    currentMethodKey.set(methodName);
  }

  public static void clear() {
    currentMethodKey.remove();
  }

  @Override
  public String inspect(String sql) {
    try {
      String methodName = currentMethodKey.get();
      if (methodName == null) return sql;

      String redisKey = CacheConstants.MONITOR_SQL_PREFIX + methodName;

      redisCache.setCacheMapValue(redisKey, "SQLDetail", sql);

      log.debug("[SQL-DETAIL] method={} | sql={}", methodName, sql);
    } catch (Exception e) {
      log.error("❌ Failed to record SQL detail", e);
    }

    return sql;
  }
}
