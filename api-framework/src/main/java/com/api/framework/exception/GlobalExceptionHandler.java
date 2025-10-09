package com.api.framework.exception;

import com.api.common.constant.HttpStatus;
import com.api.common.domain.AjaxResult;
import com.api.common.utils.Convert;
import com.api.common.utils.StringUtils;
import com.api.common.utils.html.EscapeUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Global exception handler for REST controllers.
 *
 * <p>This class provides centralized handling for all exceptions that occur during request
 * processing in the application. It logs errors consistently and returns a unified {@link
 * AjaxResult} structure to the frontend.
 *
 * <p>Supported categories include:
 *
 * <ul>
 *   <li>Security and access control errors
 *   <li>Validation and binding errors
 *   <li>Business logic exceptions
 *   <li>System and runtime exceptions
 * </ul>
 *
 * <p>Each exception method returns a JSON-formatted response with an HTTP status code and a clear
 * message describing the issue.
 *
 * @author Gavin
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handles access denied exceptions (e.g., missing roles or permissions).
   *
   * @param e the exception instance
   * @param request the HTTP request that triggered the exception
   * @return a 403 Forbidden error response
   */
  @ExceptionHandler(AccessDeniedException.class)
  public AjaxResult handleAccessDeniedException(
      AccessDeniedException e, HttpServletRequest request) {
    String requestURI = request.getRequestURI();
    log.error("Access denied. Request URI: '{}', reason: {}", requestURI, e.getMessage());
    return AjaxResult.error(
        HttpStatus.FORBIDDEN, "Access denied. Please contact the administrator for authorization.");
  }

  /**
   * Handles unsupported HTTP request methods (e.g., using POST instead of GET).
   *
   * @param e the exception instance
   * @param request the HTTP request
   * @return a response with method not supported details
   */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public AjaxResult handleHttpRequestMethodNotSupported(
      HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
    String requestURI = request.getRequestURI();
    log.error("Unsupported HTTP method. Request URI: '{}', method: {}", requestURI, e.getMethod());
    return AjaxResult.error("Unsupported request method: " + e.getMethod());
  }

  /**
   * Handles custom-defined business logic exceptions.
   *
   * @param e the {@link ServiceException} instance
   * @param request the HTTP request
   * @return a custom error message with optional error code
   */
  @ExceptionHandler(ServiceException.class)
  public AjaxResult handleServiceException(ServiceException e, HttpServletRequest request) {
    log.error("Business exception occurred: {}", e.getMessage(), e);
    Integer code = e.getCode();
    return StringUtils.isNotNull(code)
        ? AjaxResult.error(code, e.getMessage())
        : AjaxResult.error(e.getMessage());
  }

  /**
   * Handles cases where a required path variable is missing in the request.
   *
   * @param e the exception instance
   * @param request the HTTP request
   * @return an error response indicating the missing variable
   */
  @ExceptionHandler(MissingPathVariableException.class)
  public AjaxResult handleMissingPathVariableException(
      MissingPathVariableException e, HttpServletRequest request) {
    String requestURI = request.getRequestURI();
    log.error("Missing required path variable in request URI '{}'.", requestURI, e);
    return AjaxResult.error(
        String.format("Missing required path variable: '%s'", e.getVariableName()));
  }

  /**
   * Handles request parameter type mismatches.
   *
   * <p>Occurs when a query/path parameter cannot be converted to the expected type.
   *
   * @param e the exception instance
   * @param request the HTTP request
   * @return a descriptive error message with the expected type and received value
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public AjaxResult handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException e, HttpServletRequest request) {
    String requestURI = request.getRequestURI();
    String value = Convert.toStr(e.getValue());
    if (StringUtils.isNotEmpty(value)) {
      value = EscapeUtil.clean(value);
    }
    log.error(
        "Parameter type mismatch in request URI '{}'. Parameter: '{}', expected type: '{}', value: '{}'.",
        requestURI,
        e.getName(),
        e.getRequiredType(),
        value,
        e);
    return AjaxResult.error(
        String.format(
            "Invalid parameter type. Parameter '%s' should be of type '%s' but received '%s'.",
            e.getName(),
            e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown",
            value));
  }

  /**
   * Handles validation errors triggered by {@code @Valid} or {@code @Validated} annotations.
   *
   * @param e the {@link BindException} instance
   * @return an error response with the first validation message
   */
  @ExceptionHandler(BindException.class)
  public AjaxResult handleBindException(BindException e) {
    log.error("Validation error: {}", e.getMessage(), e);
    String message = e.getAllErrors().get(0).getDefaultMessage();
    return AjaxResult.error("Validation failed: " + message);
  }

  /**
   * Handles validation errors for request bodies (e.g., JSON payloads).
   *
   * @param e the {@link MethodArgumentNotValidException} instance
   * @return an error response with the validation message
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public AjaxResult handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
    log.error("Method argument validation error: {}", e.getMessage(), e);
    String message = e.getBindingResult().getFieldError().getDefaultMessage();
    return AjaxResult.error("Validation failed: " + message);
  }

  /**
   * Handles generic runtime exceptions that are not explicitly caught elsewhere.
   *
   * @param e the {@link RuntimeException} instance
   * @param request the HTTP request
   * @return an error response with the runtime error message
   */
  @ExceptionHandler(RuntimeException.class)
  public AjaxResult handleRuntimeException(RuntimeException e, HttpServletRequest request) {
    String requestURI = request.getRequestURI();
    log.error("Unexpected runtime exception occurred at '{}'.", requestURI, e);
    return AjaxResult.error("An unexpected error occurred: " + e.getMessage());
  }

  /**
   * Handles all other unclassified system-level exceptions.
   *
   * @param e the {@link Exception} instance
   * @param request the HTTP request
   * @return a system error response
   */
  @ExceptionHandler(Exception.class)
  public AjaxResult handleException(Exception e, HttpServletRequest request) {
    String requestURI = request.getRequestURI();
    log.error("Unhandled system exception at '{}'.", requestURI, e);
    return AjaxResult.error("System error: " + e.getMessage());
  }
}
