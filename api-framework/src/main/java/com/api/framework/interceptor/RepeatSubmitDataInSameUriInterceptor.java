package com.api.framework.interceptor;

import com.api.common.constant.CacheConstants;
import com.api.common.redis.RedisCache;
import com.api.common.utils.StringUtils;
import com.api.framework.annotation.RepeatSubmit;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Detects duplicate submissions based on request URI and parameters.
 *
 * <p>If the same request (URI + body/params) is received again within the configured interval, it
 * is considered a duplicate submission.
 */
@Slf4j
@Component
public class RepeatSubmitDataInSameUriInterceptor extends RepeatSubmitInterceptor {

  private static final String REPEAT_PARAMS = "repeatParams";
  private static final String REPEAT_TIME = "repeatTime";

  @Value("${token.header:Authorization}")
  private String header;

  @Autowired private RedisCache redisCache;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @SuppressWarnings("unchecked")
  protected boolean isRepeatSubmit(HttpServletRequest request, RepeatSubmit annotation) {
    try {
      String requestUri = request.getRequestURI();
      String submitKey = StringUtils.trimToEmpty(request.getHeader(header));
      String cacheKey = CacheConstants.REPEAT_SUBMIT_KEY + requestUri + ":" + submitKey;

      // Extract request body or parameters as JSON
      String currentParams = extractRequestParams(request);
      long currentTime = System.currentTimeMillis();

      Map<String, Object> currentData = new HashMap<>();
      currentData.put(REPEAT_PARAMS, currentParams);
      currentData.put(REPEAT_TIME, currentTime);

      Map<String, Object> lastData = redisCache.getCacheObject(cacheKey);
      if (lastData != null && lastData.containsKey(REPEAT_PARAMS)) {
        String lastParams = lastData.get(REPEAT_PARAMS).toString();
        long lastTime = Long.parseLong(lastData.get(REPEAT_TIME).toString());

        if (lastParams.equals(currentParams) && (currentTime - lastTime) < annotation.interval()) {
          log.debug("⚠️ Duplicate submission detected for URI: {}", requestUri);
          return true;
        }
      }

      // Cache the new request data with expiration
      redisCache.setCacheObject(
          cacheKey, currentData, annotation.interval(), TimeUnit.MILLISECONDS);
      return false;

    } catch (Exception e) {
      log.error("❌ Error while checking duplicate submission", e);
      return false;
    }
  }

  /** Extracts request parameters or body as JSON string. */
  private String extractRequestParams(HttpServletRequest request) {
    try {
      if (request.getContentType() != null
          && request.getContentType().contains("application/json")) {
        return new String(request.getInputStream().readAllBytes());
      } else {
        return objectMapper.writeValueAsString(request.getParameterMap());
      }
    } catch (Exception e) {
      log.warn("⚠️ Unable to extract request parameters", e);
      return "{}";
    }
  }
}
