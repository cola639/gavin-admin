package com.api.framework.datasource;

import lombok.extern.slf4j.Slf4j;

/** Thread-local context holder for determining the current DataSource key. */
@Slf4j
public class DynamicDataSourceContextHolder {

  private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

  /** Set the current data source key (MASTER or SLAVE). */
  public static void set(String key) {
    log.debug("➡️ Switching to DataSource: {}", key);
    CONTEXT.set(key);
  }

  /** Get the current data source key. */
  public static String get() {
    return CONTEXT.get();
  }

  /** Clear the data source key after use. */
  public static void clear() {
    CONTEXT.remove();
    log.trace("🧹 Cleared DataSource context.");
  }
}
