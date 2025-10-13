package com.api.framework.interceptor;

import com.api.common.constant.CacheConstants;
import com.api.common.domain.AjaxResult;
import com.api.common.redis.RedisCache;
import com.api.common.utils.ServletUtils;
import com.api.common.utils.StringUtils;
import com.api.framework.annotation.RepeatSubmit;
import com.api.framework.service.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Global interceptor to prevent duplicate form submissions.
 *
 * <p>It checks the same (URI + request body/params) within a time window and rejects if submitted
 * again before the interval expires.
 */
@Slf4j
@Component
public class RepeatSubmitInterceptor implements HandlerInterceptor {

  private static final String REPEAT_PARAMS = "repeatParams";
  private static final String REPEAT_TIME = "repeatTime";

  @Value("${token.header:Authorization}")
  private String header;

  @Autowired private RedisCache redisCache;

  @Autowired private TokenService tokenService;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  @SuppressWarnings("unchecked")
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {

    if (!(handler instanceof HandlerMethod handlerMethod)) {
      return true;
    }

    var method = handlerMethod.getMethod();
    var annotation = method.getAnnotation(RepeatSubmit.class);
    if (annotation == null) {
      return true;
    }

    try {
      String uri = request.getRequestURI();
      String username = tokenService.extractUsername(request);
      if (StringUtils.isEmpty(username)) {
        username = request.getRemoteAddr(); // fallback
      }

      String cacheKey = CacheConstants.REPEAT_SUBMIT_KEY + uri + ":" + username;

      // Extract request body or parameters
      String body = extractRequestBody(request);
      String params =
          StringUtils.isEmpty(body)
              ? objectMapper.writeValueAsString(request.getParameterMap())
              : body;

      Map<String, Object> current = new HashMap<>();
      current.put(REPEAT_PARAMS, params);
      current.put(REPEAT_TIME, System.currentTimeMillis());

      // ✅ Get last submission data as String (JSON)
      String lastDataJson = redisCache.getCacheObject(cacheKey);
      if (StringUtils.isNotEmpty(lastDataJson)) {
        Map<String, Object> lastData = objectMapper.readValue(lastDataJson, Map.class);

        String lastParams = String.valueOf(lastData.get(REPEAT_PARAMS));
        long lastTime = Long.parseLong(String.valueOf(lastData.get(REPEAT_TIME)));

        if (params.equals(lastParams)
            && (System.currentTimeMillis() - lastTime) < annotation.interval()) {

          log.warn("⚠️ Duplicate submission detected for URI: {} by user: {}", uri, username);
          var ajax = AjaxResult.error(annotation.message());
          ServletUtils.renderString(response, objectMapper.writeValueAsString(ajax));
          return false;
        }
      }

      // ✅ Store updated submission data as JSON in Redis
      redisCache.setCacheObject(
          cacheKey,
          objectMapper.writeValueAsString(current),
          annotation.interval(),
          TimeUnit.MILLISECONDS);

      return true;

    } catch (Exception ex) {
      log.error("❌ Failed to check duplicate submission", ex);
      return true; // fail open — allow request
    }
  }

  /** Extract request body as JSON string. */
  private String extractRequestBody(HttpServletRequest request) {
    try {
      if (request.getContentType() != null
          && request.getContentType().contains("application/json")) {
        return new String(request.getInputStream().readAllBytes());
      } else {
        return objectMapper.writeValueAsString(request.getParameterMap());
      }
    } catch (Exception e) {
      log.warn("⚠️ Failed to read request body", e);
      return "";
    }
  }
}
