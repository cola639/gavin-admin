package com.api.boot.controller.system;

import com.api.common.constant.Constants;
import com.api.common.domain.AjaxResult;
import com.api.common.domain.LoginUser;
import com.api.system.service.LoginTicketService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class SysAuthWithOosController {

  private final LoginTicketService loginTicketService;

  /**
   * Start GitHub OAuth2 login.
   *
   * <p>NOTE: Use /auth/github/start to avoid ambiguous mapping with other controller methods that
   * already map GET /auth/github.
   */
  @GetMapping("/github/start")
  public void startGithub(HttpServletResponse response) throws IOException {
    log.info("Redirecting to GitHub OAuth2 authorization endpoint.");
    response.sendRedirect("http://142.171.47.231:8989/auth2/authorization/github");
  }

  /**
   * Exchange one-time code for JWT. (If you want to keep GET for your SPA, keep it. If you later
   * want to harden it, move to POST.)
   */
  @GetMapping("/exchange")
  public AjaxResult exchange(@RequestParam("code") String code) {
    String jwt = loginTicketService.consume(code);
    if (jwt == null || jwt.isBlank()) {
      return AjaxResult.error("Invalid or expired code");
    }
    AjaxResult ajax = AjaxResult.success();
    ajax.put(Constants.TOKEN, jwt);
    return ajax;
  }

  /**
   * Current user info (requires JWT). Your SecurityConfig protects /api/** by default, so keep this
   * under /api if that's your standard.
   */
  @GetMapping("/me")
  public Map<String, Object> me(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return Map.of("authenticated", false);
    }

    Object principal = authentication.getPrincipal();

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("authenticated", true);
    result.put("username", authentication.getName());
    result.put("authorities", authentication.getAuthorities());

    // If your JWT filter sets principal as LoginUser, return more useful fields
    if (principal instanceof LoginUser loginUser) {
      result.put("userId", loginUser.getUserId());
      if (loginUser.getUser() != null) {
        result.put("nickName", loginUser.getUser().getNickName());
        result.put("deptId", loginUser.getDeptId());
      }
      result.put("loginType", loginUser.getLoginType());
      result.put("oauth2Provider", loginUser.getOauth2Provider());
      result.put("oauth2UserId", loginUser.getOauth2UserId());
    } else {
      // Fallback: show principal class (debug-friendly)
      result.put("principalType", principal == null ? null : principal.getClass().getName());
      result.put("principal", Objects.toString(principal, null));
    }

    return result;
  }
}
