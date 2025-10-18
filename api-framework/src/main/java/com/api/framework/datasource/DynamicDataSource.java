package com.api.framework.datasource;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.Map;

/**
 * A dynamic routing DataSource that determines which DataSource to use (MASTER or SLAVE) at runtime
 * using ThreadLocal context.
 */
@Slf4j
public class DynamicDataSource extends AbstractRoutingDataSource {

  public DynamicDataSource(
      @NonNull DataSource defaultDataSource, @NonNull Map<Object, Object> targetDataSources) {

    super.setDefaultTargetDataSource(defaultDataSource);
    super.setTargetDataSources(targetDataSources);
    super.afterPropertiesSet();

    log.info("✅ DynamicDataSource initialized. Available targets: {}", targetDataSources.keySet());
  }

  @Override
  protected Object determineCurrentLookupKey() {
    String lookupKey = DynamicDataSourceContextHolder.get();
    if (lookupKey == null) {
      log.trace("Using default DataSource → MASTER");
      return "MASTER";
    }
    log.debug("🔄 Routing to DataSource: {}", lookupKey);
    return lookupKey;
  }
}
