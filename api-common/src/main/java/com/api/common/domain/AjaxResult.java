package com.api.common.domain;

import com.api.common.constant.HttpStatus;
import com.api.common.utils.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;

/**
 * Standard response wrapper for REST APIs. Provides a unified structure for returning status,
 * messages, and data.
 *
 * <p>Usage examples:
 *
 * <pre>
 *     return AjaxResult.success();
 *     return AjaxResult.success("Operation completed", userData);
 *     return AjaxResult.error("Invalid request"下·下·);
 * </pre>
 *
 * @author
 */
public class AjaxResult<T> extends HashMap<String, Object> implements Serializable {

  private static final long serialVersionUID = 1L;

  /** Status code key */
  public static final String CODE_TAG = "code";

  /** Message key */
  public static final String MSG_TAG = "msg";

  /** Data key */
  public static final String DATA_TAG = "data";

  /** Default constructor. */
  public AjaxResult() {}

  /**
   * Constructor with status and message.
   *
   * @param code status code
   * @param msg message
   */
  public AjaxResult(int code, String msg) {
    super.put(CODE_TAG, code);
    super.put(MSG_TAG, msg);
  }

  /**
   * Constructor with status, message and data.
   *
   * @param code status code
   * @param msg message
   * @param data data object
   */
  public AjaxResult(int code, String msg, T data) {
    super.put(CODE_TAG, code);
    super.put(MSG_TAG, msg);
    if (StringUtils.isNotNull(data)) {
      super.put(DATA_TAG, data);
    }
  }

  // ---------- Static factory methods ----------

  public static <T> AjaxResult<T> success() {
    return success("Operation successful");
  }

  public static <T> AjaxResult<T> success(T data) {
    return success("Operation successful", data);
  }

  public static <T> AjaxResult<T> success(String msg) {
    return success(msg, null);
  }

  public static <T> AjaxResult<T> success(String msg, T data) {
    return new AjaxResult<>(HttpStatus.SUCCESS, msg, data);
  }

  public static <T> AjaxResult<T> warn(String msg) {
    return warn(msg, null);
  }

  public static <T> AjaxResult<T> warn(String msg, T data) {
    return new AjaxResult<>(HttpStatus.WARN, msg, data);
  }

  public static <T> AjaxResult<T> error() {
    return error("Operation failed");
  }

  public static <T> AjaxResult<T> error(String msg) {
    return error(msg, null);
  }

  public static <T> AjaxResult<T> error(String msg, T data) {
    return new AjaxResult<>(HttpStatus.ERROR, msg, data);
  }

  public static <T> AjaxResult<T> error(int code, String msg) {
    return new AjaxResult<>(code, msg, null);
  }

  // ---------- Status check methods ----------

  public boolean isSuccess() {
    return Objects.equals(HttpStatus.SUCCESS, this.get(CODE_TAG));
  }

  public boolean isWarn() {
    return Objects.equals(HttpStatus.WARN, this.get(CODE_TAG));
  }

  public boolean isError() {
    return Objects.equals(HttpStatus.ERROR, this.get(CODE_TAG));
  }

  // ---------- Fluent API ----------

  @Override
  public AjaxResult<T> put(String key, Object value) {
    super.put(key, value);
    return this;
  }
}
