package com.api.framework.annotation;

import java.lang.annotation.*;

/**
 * Annotation to prevent duplicate form submissions within a given time window.
 *
 * <p>Example usage:
 *
 * <pre>
 *     @PostMapping("/submit")
 *     @RepeatSubmit(interval = 5000, message = "Duplicate submission detected")
 *     public AjaxResult submitForm(@RequestBody FormData form) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface RepeatSubmit {

  /**
   * Minimum interval between two submissions in milliseconds. Submissions within this time window
   * are considered duplicates.
   */
  int interval() default 5000;

  /** Message returned when a duplicate submission is detected. */
  String message() default "Duplicate submission detected, please try again later.";
}
