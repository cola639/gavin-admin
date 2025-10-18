package com.api.framework.config;

import com.api.framework.datasource.DynamicDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/** Configures multiple data sources and registers the dynamic router. */
@Slf4j
@Configuration
public class DynamicDataSourceConfig {

  @Bean
  @ConfigurationProperties("spring.datasource.master")
  public DataSourceProperties masterDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean
  @ConfigurationProperties("spring.datasource.slave")
  public DataSourceProperties slaveDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean("masterDataSource")
  public DataSource masterDataSource(
      @Qualifier("masterDataSourceProperties") DataSourceProperties props) {
    log.info("✅ Initializing MASTER data source...");
    return props.initializeDataSourceBuilder().build();
  }

  @Bean("slaveDataSource")
  public DataSource slaveDataSource(
      @Qualifier("slaveDataSourceProperties") DataSourceProperties props) {
    log.info("✅ Initializing SLAVE data source...");
    return props.initializeDataSourceBuilder().build();
  }

  @Primary
  @Bean("dynamicDataSource")
  public DynamicDataSource dynamicDataSource(
      @Qualifier("masterDataSource") DataSource master,
      @Qualifier("slaveDataSource") DataSource slave) {
    Map<Object, Object> targets = new HashMap<>();
    targets.put("MASTER", master);
    targets.put("SLAVE", slave);

    log.info("🔧 Dynamic data source configured successfully.");
    return new DynamicDataSource(master, targets);
  }
}
