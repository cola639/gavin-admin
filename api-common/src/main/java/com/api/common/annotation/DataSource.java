package com.api.common.annotation;

import com.api.common.enums.DataSourceType;

import java.lang.annotation.*;

/**
 * Annotation for switching between multiple data sources.
 *
 * <p>Priority order: 1️⃣ Method-level annotation overrides class-level annotation. 2️⃣ If not
 * specified at method-level, class-level is used.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface DataSource {

  /** Target data source type (MASTER by default). */
  DataSourceType value() default DataSourceType.MASTER;
}
