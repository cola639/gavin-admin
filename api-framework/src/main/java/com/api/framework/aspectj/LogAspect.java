package com.api.framework.aspectj;

import com.api.common.annotation.Log;
import com.api.common.enums.BusinessStatus;
import com.api.common.utils.ServletUtils;
import com.api.common.utils.StringUtils;
import com.api.common.utils.ip.IpUtils;
import com.api.framework.manger.AsyncFactory;
import com.api.persistence.domain.system.SysOperLog;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * Aspect for recording operation logs.
 *
 * <p>Captures method execution annotated with {@link Log} and stores structured operation data
 * asynchronously.
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

  private static final String[] EXCLUDE_PROPERTIES = {
    "password", "oldPassword", "newPassword", "confirmPassword"
  };

  private static final ThreadLocal<Long> TIME_THREADLOCAL = new ThreadLocal<>();
  private final ObjectMapper objectMapper;

  public LogAspect(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper.copy().setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  /** Before executing method — record start time */
  @Before("@annotation(controllerLog)")
  public void doBefore(JoinPoint joinPoint, Log controllerLog) {
    TIME_THREADLOCAL.set(System.currentTimeMillis());
  }

  /** After successful execution */
  @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
  public void doAfterReturning(JoinPoint joinPoint, Log controllerLog, Object jsonResult) {
    handleLog(joinPoint, controllerLog, null, jsonResult);
  }

  /** After throwing exception */
  @AfterThrowing(value = "@annotation(controllerLog)", throwing = "e")
  public void doAfterThrowing(JoinPoint joinPoint, Log controllerLog, Exception e) {
    handleLog(joinPoint, controllerLog, e, null);
  }

  private void handleLog(
      final JoinPoint joinPoint, Log controllerLog, final Exception e, Object jsonResult) {
    try {
      SysOperLog operLog = new SysOperLog();
      operLog.setStatus(BusinessStatus.SUCCESS.ordinal());
      operLog.setOperIp(IpUtils.getIpAddr());
      operLog.setOperUrl(ServletUtils.getRequest().getRequestURI());
      operLog.setMethod(
          joinPoint.getTarget().getClass().getName()
              + "."
              + joinPoint.getSignature().getName()
              + "()");

      if (e != null) {
        operLog.setStatus(BusinessStatus.FAIL.ordinal());
        operLog.setErrorMsg(StringUtils.truncate(e.getMessage(), 2000));
      }

      operLog.setRequestMethod(ServletUtils.getRequest().getMethod());
      fillLogDetails(joinPoint, controllerLog, operLog, jsonResult);

      operLog.setCostTime(System.currentTimeMillis() - TIME_THREADLOCAL.get());

    } catch (Exception ex) {
      log.error("Failed to record operation log: {}", ex.getMessage(), ex);
    } finally {
      TIME_THREADLOCAL.remove();
    }
  }

  /** Extracts method description and parameters from @Log annotation. */
  private void fillLogDetails(
      JoinPoint joinPoint, Log logAnn, SysOperLog operLog, Object jsonResult) {
    operLog.setBusinessType(logAnn.businessType().ordinal());
    operLog.setTitle(logAnn.title());
    operLog.setOperatorType(logAnn.operatorType().ordinal());

    if (logAnn.isSaveRequestData()) {
      String params = collectRequestParams(joinPoint, logAnn.excludeParamNames());
      operLog.setOperParam(StringUtils.truncate(params, 2000));
    }

    if (logAnn.isSaveResponseData() && jsonResult != null) {
      operLog.setJsonResult(StringUtils.truncate(toJsonSafe(jsonResult), 2000));
    }
  }

  /** Collects request parameters safely and filters sensitive data. */
  private String collectRequestParams(JoinPoint joinPoint, String[] excludeNames) {
    HttpServletRequest request = ServletUtils.getRequest();
    Map<String, ?> paramMap = ServletUtils.getParamMap(request);

    try {
      if ((paramMap == null || paramMap.isEmpty())
          && List.of(HttpMethod.POST.name(), HttpMethod.PUT.name(), HttpMethod.DELETE.name())
              .contains(request.getMethod())) {
        return argsToJson(joinPoint.getArgs(), excludeNames);
      } else {
        return objectMapper.writeValueAsString(paramMap);
      }
    } catch (Exception e) {
      log.warn("Failed to serialize request params: {}", e.getMessage());
      return "{}";
    }
  }

  /** Converts method arguments to JSON safely. */
  private String argsToJson(Object[] args, String[] excludeNames) {
    StringBuilder params = new StringBuilder();
    for (Object arg : args) {
      if (arg != null && !isFilterObject(arg)) {
        try {
          params.append(objectMapper.writeValueAsString(arg)).append(' ');
        } catch (Exception ignored) {
        }
      }
    }
    return params.toString().trim();
  }

  /** Determines if the argument should be excluded from logging. */
  @SuppressWarnings("rawtypes")
  private boolean isFilterObject(final Object obj) {
    Class<?> clazz = obj.getClass();
    if (clazz.isArray()) {
      return MultipartFile.class.isAssignableFrom(clazz.getComponentType());
    } else if (Collection.class.isAssignableFrom(clazz)) {
      for (Object value : (Collection) obj) {
        if (value instanceof MultipartFile) return true;
      }
    } else if (Map.class.isAssignableFrom(clazz)) {
      for (Object value : ((Map<?, ?>) obj).values()) {
        if (value instanceof MultipartFile) return true;
      }
    }
    return obj instanceof MultipartFile
        || obj instanceof HttpServletRequest
        || obj instanceof HttpServletResponse
        || obj instanceof BindingResult;
  }

  private String toJsonSafe(Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (Exception e) {
      log.debug("JSON serialization failed: {}", e.getMessage());
      return "{}";
    }
  }
}
