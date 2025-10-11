package com.api.framework.annotation;

import java.lang.annotation.*;

/**
 * Annotation to automatically track SQL execution performance.
 *
 * <p>If {@code value()} is not provided, the method name will be used as identifier.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TrackSQLDetail {

  /** The SQL statement (optional). If empty, the method name will be used instead. */
  String value() default "";
}
