package com.api.framework.security.handle;

import com.api.common.constant.Constants;
import com.api.common.domain.AjaxResult;
import com.api.common.domain.LoginUser;
import com.api.common.utils.MessageUtils;
import com.api.common.utils.ServletUtils;
import com.api.common.utils.StringUtils;
import com.api.framework.service.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom logout success handler.
 *
 * <p>- Removes user session token from Redis - Records logout logs asynchronously - Returns JSON
 * response on successful logout
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {

  private final TokenService tokenService;
  private final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Handles successful logout events.
   *
   * @param request HTTP request
   * @param response HTTP response
   * @param authentication Spring Security authentication object
   */
  @Override
  public void onLogoutSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {

    LoginUser loginUser = tokenService.getLoginUser(request);

    if (StringUtils.isNotNull(loginUser)) {
      String userName = loginUser.getUsername();

      // Remove user cache record
      tokenService.delLoginUser(loginUser.getToken());

      // Record logout log asynchronously
      //      AsyncManager.me()
      //          .execute(
      //              AsyncFactory.recordLogininfor(
      //                  userName, Constants.LOGOUT, MessageUtils.message("user.logout.success")));

      log.info("User [{}] logged out successfully", userName);
    }

    // Return success response as JSON
    String jsonResponse =
        objectMapper.writeValueAsString(
            AjaxResult.success(MessageUtils.message("user.logout.success")));
    ServletUtils.renderString(response, jsonResponse);
  }
}
