package com.api.system.aspectj;

import com.api.common.annotation.Log;
import com.api.common.enums.BusinessStatus;
import com.api.common.utils.ServletUtils;
import com.api.common.utils.StringUtils;
import com.api.common.utils.ip.IpUtils;
import com.api.persistence.domain.system.SysOperLog;
import com.api.system.manager.AsyncFactory;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * Aspect for capturing and recording operation logs.
 *
 * <p>This aspect intercepts methods annotated with {@link Log}, collects contextual data (request
 * info, parameters, exceptions, etc.), and saves the record asynchronously via {@link
 * AsyncFactory}.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

  private static final String[] EXCLUDE_PROPERTIES = {
    "password", "oldPassword", "newPassword", "confirmPassword"
  };

  private static final ThreadLocal<Long> TIME_THREADLOCAL = new ThreadLocal<>();

  private final ObjectMapper objectMapper;
  private final AsyncFactory asyncFactory;

  /** Before executing method — record the start timestamp. */
  @Before("@annotation(controllerLog)")
  public void doBefore(JoinPoint joinPoint, Log controllerLog) {
    TIME_THREADLOCAL.set(System.currentTimeMillis());
  }

  /** After successful execution — handle normal log recording. */
  @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
  public void doAfterReturning(JoinPoint joinPoint, Log controllerLog, Object jsonResult) {
    handleLog(joinPoint, controllerLog, null, jsonResult);
  }

  /** After an exception is thrown — handle failed log recording. */
  @AfterThrowing(value = "@annotation(controllerLog)", throwing = "e")
  public void doAfterThrowing(JoinPoint joinPoint, Log controllerLog, Exception e) {
    handleLog(joinPoint, controllerLog, e, null);
  }

  /** Central log handler for both normal and exceptional outcomes. */
  private void handleLog(JoinPoint joinPoint, Log controllerLog, Exception e, Object jsonResult) {
    try {
      SysOperLog operLog = buildOperLog(joinPoint, controllerLog, e, jsonResult);

      // ✅ Save asynchronously using AsyncFactory
      asyncFactory.recordOper(operLog);

      log.debug("📝 Operation log submitted asynchronously: {}", operLog.getTitle());
    } catch (Exception ex) {
      log.error("❌ Failed to record operation log: {}", ex.getMessage(), ex);
    } finally {
      TIME_THREADLOCAL.remove();
    }
  }

  /** Builds a SysOperLog entity based on the method context and annotation configuration. */
  private SysOperLog buildOperLog(JoinPoint joinPoint, Log logAnn, Exception e, Object jsonResult) {
    SysOperLog logEntity = new SysOperLog();
    logEntity.setTitle(logAnn.title());
    logEntity.setBusinessType(logAnn.businessType().ordinal());
    logEntity.setOperatorType(logAnn.operatorType().ordinal());
    logEntity.setStatus(
        e == null ? BusinessStatus.SUCCESS.ordinal() : BusinessStatus.FAIL.ordinal());
    logEntity.setOperIp(IpUtils.getIpAddr());
    logEntity.setOperUrl(ServletUtils.getRequest().getRequestURI());
    logEntity.setMethod(
        joinPoint.getTarget().getClass().getName()
            + "."
            + joinPoint.getSignature().getName()
            + "()");
    logEntity.setRequestMethod(ServletUtils.getRequest().getMethod());
    logEntity.setCostTime(System.currentTimeMillis() - TIME_THREADLOCAL.get());

    if (e != null) {
      logEntity.setErrorMsg(StringUtils.truncate(e.getMessage(), 2000));
    }

    fillLogDetails(joinPoint, logAnn, logEntity, jsonResult);
    return logEntity;
  }

  /** Extracts request/response information and attaches it to the operation log. */
  private void fillLogDetails(
      JoinPoint joinPoint, Log logAnn, SysOperLog operLog, Object jsonResult) {
    try {
      if (logAnn.isSaveRequestData()) {
        String params = collectRequestParams(joinPoint, logAnn.excludeParamNames());
        operLog.setOperParam(StringUtils.truncate(params, 2000));
      }

      if (logAnn.isSaveResponseData() && jsonResult != null) {
        operLog.setJsonResult(StringUtils.truncate(toJsonSafe(jsonResult), 2000));
      }
    } catch (Exception ex) {
      log.warn("⚠️ Failed to extract request/response data: {}", ex.getMessage());
    }
  }

  /** Collects and serializes request parameters safely, filtering sensitive or excluded fields. */
  private String collectRequestParams(JoinPoint joinPoint, String[] excludeNames) {
    HttpServletRequest request = ServletUtils.getRequest();
    Map<String, ?> paramMap = ServletUtils.getParamMap(request);

    try {
      if ((paramMap == null || paramMap.isEmpty())
          && List.of(HttpMethod.POST.name(), HttpMethod.PUT.name(), HttpMethod.DELETE.name())
              .contains(request.getMethod())) {
        return argsToJson(joinPoint.getArgs(), excludeNames);
      }
      return objectMapper
          .copy()
          .setSerializationInclusion(JsonInclude.Include.NON_NULL)
          .writeValueAsString(paramMap);
    } catch (Exception e) {
      log.warn("Failed to serialize request parameters: {}", e.getMessage());
      return "{}";
    }
  }

  /** Serializes method arguments to JSON while skipping non-serializable or filtered types. */
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

  /** Determines whether an argument should be excluded from logging. */
  @SuppressWarnings("rawtypes")
  private boolean isFilterObject(final Object obj) {
    if (obj == null) return true;
    Class<?> clazz = obj.getClass();

    if (clazz.isArray()) {
      return MultipartFile.class.isAssignableFrom(clazz.getComponentType());
    }
    if (Collection.class.isAssignableFrom(clazz)) {
      for (Object value : (Collection) obj) {
        if (value instanceof MultipartFile) return true;
      }
    }
    if (Map.class.isAssignableFrom(clazz)) {
      for (Object value : ((Map<?, ?>) obj).values()) {
        if (value instanceof MultipartFile) return true;
      }
    }
    return obj instanceof MultipartFile
        || obj instanceof HttpServletRequest
        || obj instanceof HttpServletResponse
        || obj instanceof BindingResult;
  }

  /** Safely converts an object to JSON using Jackson. */
  private String toJsonSafe(Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (Exception e) {
      log.debug("JSON serialization failed: {}", e.getMessage());
      return "{}";
    }
  }
}
