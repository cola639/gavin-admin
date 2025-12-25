package com.api.system.security;

import com.api.common.constant.Constants;
import com.api.common.domain.AjaxResult;
import com.api.common.domain.LoginUser;
import com.api.common.domain.SysUser;
import com.api.common.enums.DelFlagEnum;
import com.api.common.enums.StatusEnum;
import com.api.framework.service.TokenService;
import com.api.system.repository.SysUserRepository;
import com.api.system.service.LoginTicketService;
import com.api.system.service.SysPermissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component // ✅ no explicit bean name
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

  private static final String ATTR_ID = "id";
  private static final String ATTR_LOGIN = "login";
  private static final String ATTR_EMAIL = "email";

  private static final String HEADER_ACCEPT = "Accept";
  private static final String HEADER_X_REQUESTED_WITH = "X-Requested-With";
  private static final String XML_HTTP_REQUEST = "XMLHttpRequest";
  private static final String PARAM_DIRECT = "direct";

  private static final String JSON_UTF8 = "application/json;charset=UTF-8";
  private static final int MAX_USERNAME_SUFFIX = 9999;

  private final TokenService tokenService;
  private final SysUserRepository sysUserRepository;
  private final SysPermissionService permissionService;
  private final PasswordEncoder passwordEncoder;
  private final LoginTicketService loginTicketService;
  private final ObjectMapper objectMapper;

  @Value("${spring.security.oauth2.client.registration.github.frontend-redirect}")
  private String frontendRedirect;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {

    if (!(authentication instanceof OAuth2AuthenticationToken oauth2Token)) {
      log.warn(
          "Unexpected authentication type: {}",
          authentication == null ? "null" : authentication.getClass().getName());
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    OAuth2User oauth2User = oauth2Token.getPrincipal();

    String githubId = attributeAsString(oauth2User, ATTR_ID, oauth2User.getName());
    String githubLogin = attributeAsString(oauth2User, ATTR_LOGIN, "github_" + githubId);
    String email =
        attributeAsString(oauth2User, ATTR_EMAIL, null); // may be null by privacy settings

    SysUser sysUser = findOrCreateLocalUser(githubLogin, email, githubId);
    LoginUser loginUser = buildLoginUser(sysUser, githubId);

    String jwt = tokenService.createToken(loginUser);
    log.info("GitHub login success: githubId={}, userId={}", githubId, sysUser.getUserId());

    if (wantsJson(request)) {
      writeAjaxToken(response, jwt);
      return;
    }

    redirectWithTicket(response, jwt);
  }

  private LoginUser buildLoginUser(SysUser sysUser, String githubId) {
    Set<String> permissions = permissionService.getMenuPermission(sysUser);
    return LoginUser.builder()
        .userId(sysUser.getUserId())
        .deptId(sysUser.getDeptId())
        .user(sysUser)
        .permissions(permissions)
        .loginType("GITHUB")
        .oauth2Provider("github")
        .oauth2UserId(githubId)
        .build();
  }

  private void redirectWithTicket(HttpServletResponse response, String jwt) throws IOException {
    String code = loginTicketService.issue(jwt);
    String redirectUrl =
        UriComponentsBuilder.fromUriString(frontendRedirect)
            .queryParam("code", code)
            .build()
            .toUriString();

    response.sendRedirect(redirectUrl);
  }

  private boolean wantsJson(HttpServletRequest request) {
    String accept = request.getHeader(HEADER_ACCEPT);
    String xrw = request.getHeader(HEADER_X_REQUESTED_WITH);
    String direct = request.getParameter(PARAM_DIRECT);

    return "true".equalsIgnoreCase(direct)
        || XML_HTTP_REQUEST.equalsIgnoreCase(xrw)
        || (accept != null && accept.toLowerCase().contains("application/json"));
  }

  private void writeAjaxToken(HttpServletResponse response, String jwt) throws IOException {
    AjaxResult ajax = AjaxResult.success();
    ajax.put(Constants.TOKEN, jwt);

    response.setStatus(HttpServletResponse.SC_OK);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType(JSON_UTF8);
    objectMapper.writeValue(response.getWriter(), ajax);
  }

  private SysUser findOrCreateLocalUser(String githubLogin, String email, String githubId) {
    final String delFlagNormal = DelFlagEnum.NORMAL.getCode();

    String baseUserName =
        (githubLogin == null || githubLogin.isBlank())
            ? ("github_" + githubId)
            : githubLogin.trim();

    if (email != null && !email.isBlank()) {
      String normalizedEmail = email.trim();
      return sysUserRepository
          .findByEmailAndDelFlag(normalizedEmail, delFlagNormal)
          .orElseGet(
              () ->
                  createGithubUser(ensureUniqueUserName(baseUserName), normalizedEmail, githubId));
    }

    return sysUserRepository
        .findByUserNameAndDelFlag(baseUserName, delFlagNormal)
        .orElseGet(() -> createGithubUser(ensureUniqueUserName(baseUserName), null, githubId));
  }

  private String ensureUniqueUserName(String base) {
    String normalized = (base == null || base.isBlank()) ? "github_user" : base.trim();

    if (!sysUserRepository.existsByUserName(normalized)) {
      return normalized;
    }

    for (int i = 1; i <= MAX_USERNAME_SUFFIX; i++) {
      String candidate = normalized + "_" + i;
      if (!sysUserRepository.existsByUserName(candidate)) {
        log.info("Generated unique username for GitHub login: {}", candidate);
        return candidate;
      }
    }

    throw new IllegalStateException("Failed to generate unique username for GitHub login");
  }

  private SysUser createGithubUser(String username, String email, String githubId) {
    String finalUsername =
        (username == null || username.isBlank()) ? ("github_" + githubId) : username.trim();

    SysUser u = new SysUser();
    u.setUserName(finalUsername);
    u.setEmail(email);
    u.setNickName(finalUsername);
    // Random password so DB constraints pass; user won't use it.
    u.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

    u.setDelFlag(DelFlagEnum.NORMAL.getCode());
    u.setStatus(StatusEnum.ENABLED.getCode());
    u.setAvatar("");

    SysUser saved = sysUserRepository.save(u);
    log.info(
        "Created local user for GitHub login: userId={}, githubId={}", saved.getUserId(), githubId);
    return saved;
  }

  private String attributeAsString(OAuth2User user, String attribute, String defaultValue) {
    Object value = user.getAttribute(attribute);
    if (value == null) {
      return defaultValue;
    }
    String s = Objects.toString(value, defaultValue);
    return (s == null || s.isBlank()) ? defaultValue : s;
  }
}
