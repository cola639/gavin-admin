package com.api.common.annotation;

import com.api.common.enums.LogBusinessType;
import com.api.common.enums.OperatorType;

import java.lang.annotation.*;

/**
 * Custom annotation for operation logging.
 *
 * <p>Used on Controller or Service methods to record operation logs automatically.
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

  /** Module name */
  String title() default "";

  /** Business operation type */
  LogBusinessType businessType() default LogBusinessType.OTHER;

  /** Operator category */
  OperatorType operatorType() default OperatorType.MANAGE;

  /** Whether to save request parameters */
  boolean isSaveRequestData() default true;

  /** Whether to save response data */
  boolean isSaveResponseData() default true;

  /** Exclude specified parameter names from logging */
  String[] excludeParamNames() default {};
}
