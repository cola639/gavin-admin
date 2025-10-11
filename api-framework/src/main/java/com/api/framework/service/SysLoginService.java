package com.api.framework.service;

import com.api.common.constant.CacheConstants;
import com.api.common.constant.Constants;
import com.api.common.constant.UserConstants;
import com.api.common.domain.LoginUser;
import com.api.common.redis.RedisCache;
import com.api.common.utils.MessageUtils;
import com.api.common.utils.StringUtils;
import com.api.framework.exception.user.*;
import com.api.framework.security.context.AuthenticationContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Service for handling login authentication and validation.
 *
 * <p>Responsibilities: - Validate captcha codes. - Perform pre-checks before authentication (e.g.,
 * input validation, IP blacklist). - Authenticate user credentials using Spring Security. - Record
 * login attempts asynchronously (success/failure). - Issue and return JWT tokens for authenticated
 * users.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysLoginService {

  private final TokenService tokenService;
  private final AuthenticationManager authenticationManager;
  private final RedisCache redisCache;

  //  private final  userService;
  //  private final ISysConfigService configService;

  /**
   * Perform login for a user.
   *
   * @param username username
   * @param password password
   * @param code captcha code
   * @param uuid captcha UUID
   * @return JWT token
   */
  public String login(String username, String password, String code, String uuid) {
    // Validate captcha if enabled
    validateCaptcha(username, code, uuid);

    // Perform pre-checks (username/password validity, blacklist, etc.)
    loginPreCheck(username, password);

    Authentication authentication;
    try {
      UsernamePasswordAuthenticationToken authToken =
          new UsernamePasswordAuthenticationToken(username, password);

      AuthenticationContextHolder.setContext(authToken);
      // This triggers UserDetailsServiceImpl.loadUserByUsername
      authentication = authenticationManager.authenticate(authToken);

    } catch (Exception e) {
      handleAuthenticationFailure(username, e);
      throw e;
    }

    //    // Log successful login
    //    AsyncManager.me()
    //        .execute(
    //            AsyncFactory.recordLogininfor(
    //                username, Constants.LOGIN_SUCCESS,
    // MessageUtils.message("user.login.success")));

    LoginUser loginUser = (LoginUser) authentication.getPrincipal();
    recordLoginInfo(loginUser.getUserId());

    // Return generated JWT
    return tokenService.createToken(loginUser);
  }

  /** Handle authentication failures. */
  private void handleAuthenticationFailure(String username, Exception e) {
    //    if (e instanceof BadCredentialsException) {
    //      AsyncManager.me()
    //          .execute(
    //              AsyncFactory.recordLogininfor(
    //                  username, Constants.LOGIN_FAIL,
    // MessageUtils.message("user.password.not.match")));
    //      throw new UserPasswordNotMatchException();
    //    } else {
    //      AsyncManager.me()
    //          .execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL,
    // e.getMessage()));
    //      throw new ServiceException(e.getMessage());
    //    }
  }

  /** Validate captcha input. */
  public void validateCaptcha(String username, String code, String uuid) {
    //    if (!configService.selectCaptchaEnabled()) {
    //      return;
    //    }
    //
    //    String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
    //    String captcha = redisCache.getCacheObject(verifyKey);
    //
    //    if (captcha == null) {
    //      AsyncManager.me()
    //          .execute(
    //              AsyncFactory.recordLogininfor(
    //                  username, Constants.LOGIN_FAIL,
    // MessageUtils.message("user.jcaptcha.expire")));
    //      throw new CaptchaExpireException();
    //    }
    //
    //    redisCache.deleteObject(verifyKey);
    //
    //    if (!code.equalsIgnoreCase(captcha)) {
    //      AsyncManager.me()
    //          .execute(
    //              AsyncFactory.recordLogininfor(
    //                  username, Constants.LOGIN_FAIL,
    // MessageUtils.message("user.jcaptcha.error")));
    //      throw new CaptchaException();
    //    }
  }

  /** Pre-check before login (input, length validation, blacklist check). */
  public void loginPreCheck(String username, String password) {
    //    if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
    //      AsyncManager.me()
    //          .execute(
    //              AsyncFactory.recordLogininfor(
    //                  username, Constants.LOGIN_FAIL, MessageUtils.message("not.null")));
    //      throw new UserNotExistsException();
    //    }
    //
    //    if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
    //        || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
    //      throwInvalidPassword(username);
    //    }
    //
    //    if (username.length() < UserConstants.USERNAME_MIN_LENGTH
    //        || username.length() > UserConstants.USERNAME_MAX_LENGTH) {
    //      throwInvalidPassword(username);
    //    }
    //
    //    // Blacklist check
    //    String blackStr = configService.selectConfigByKey("sys.login.blackIPList");
    //    if (IpUtils.isMatchedIp(blackStr, IpUtils.getIpAddr())) {
    //      AsyncManager.me()
    //          .execute(
    //              AsyncFactory.recordLogininfor(
    //                  username, Constants.LOGIN_FAIL, MessageUtils.message("login.blocked")));
    //      throw new BlackListException();
    //    }
  }

  private void throwInvalidPassword(String username) {
    //    AsyncManager.me()
    //        .execute(
    //            AsyncFactory.recordLogininfor(
    //                username, Constants.LOGIN_FAIL,
    // MessageUtils.message("user.password.not.match")));
    //    throw new UserPasswordNotMatchException();
  }

  /** Record login info (last login IP + timestamp). */
  public void recordLoginInfo(Long userId) {
    //    userService.updateLoginInfo(userId, IpUtils.getIpAddr(), DateUtils.getNowDate());
  }
}
