package com.api.framework.security.handle;

import com.api.common.domain.AjaxResult;
import com.api.common.utils.ServletUtils;
import com.api.common.utils.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Handles authentication failures by returning an HTTP 401 Unauthorized response. */
@Slf4j
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {

    int code = HttpStatus.UNAUTHORIZED.value();
    String msg =
        StringUtils.format(
            "Request URI: {} - Authentication failed, unable to access system resources",
            request.getRequestURI());

    log.warn("Unauthorized access attempt: {}", msg, authException);

    String body = objectMapper.writeValueAsString(AjaxResult.error(code, msg));
    ServletUtils.renderString(response, body);
  }
}
