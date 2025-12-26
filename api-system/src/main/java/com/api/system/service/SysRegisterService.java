package com.api.system.service;

import com.api.common.constant.CacheConstants;
import com.api.common.constant.UserConstants;
import com.api.common.domain.RegisterBody;
import com.api.common.domain.SysUser;
import com.api.common.enums.DelFlagEnum;
import com.api.common.redis.RedisCache;
import com.api.common.utils.DateEnhancedUtil;
import com.api.common.utils.SecurityUtils;
import com.api.common.utils.StringUtils;
import com.api.framework.exception.user.CaptchaException;
import com.api.framework.exception.user.CaptchaExpireException;
import com.api.system.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for user registration.
 *
 * <p>- Handles user validation (username, password, uniqueness). - Validates captcha (if enabled).
 * - Encrypts password and saves new user. - Records login information asynchronously after
 * successful registration.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysRegisterService {

  private final SysUserRepository userRepository;
  private final RedisCache redisCache;

  /**
   * Handles user registration.
   *
   * @param registerBody Registration request containing username, password, captcha, etc.
   * @return message indicating success or failure
   */
  public String register(RegisterBody registerBody) {
    String email = registerBody.getEmail();
    String password = registerBody.getPassword();
    SysUser sysUser = new SysUser();
    sysUser.setUserName(email);

    // Check captcha if enabled
    //    if (configService.selectCaptchaEnabled()) {
    //      validateCaptcha(email, registerBody.getCode(), registerBody.getUuid());
    //    }
    //
    String msg = validateUserInput(email, password, sysUser);
    if (StringUtils.isNotEmpty(msg)) {
      return msg;
    }

    // Set user details
    sysUser.setDelFlag(DelFlagEnum.NORMAL.getCode());
    sysUser.setNickName(email);
    sysUser.setPwdUpdateDate(DateEnhancedUtil.getNowDate());
    sysUser.setPassword(SecurityUtils.encryptPassword(password));

    // Save user
    SysUser registered = userRepository.save(sysUser);
    if (registered.getUserId() == null) {
      return "Registration failed. Please contact system administrator.";
    }

    // Record successful registration asynchronously
    //    AsyncManager.me()
    //        .execute(
    //            AsyncFactory.recordLogininfor(
    //                username, Constants.REGISTER, MessageUtils.message("user.register.success")));

    log.info("User [{}] registered successfully.", email);
    return "";
  }

  /**
   * Validate captcha against Redis stored value.
   *
   * @param username Username
   * @param code Captcha input
   * @param uuid Unique identifier
   */
  private void validateCaptcha(String username, String code, String uuid) {
    String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
    String captcha = redisCache.getCacheObject(verifyKey);

    // Delete captcha from cache after validation
    redisCache.deleteObject(verifyKey);

    if (captcha == null) {
      log.warn("Captcha expired for user [{}]", username);
      throw new CaptchaExpireException();
    }
    if (!code.equalsIgnoreCase(captcha)) {
      log.warn("Invalid captcha for user [{}]", username);
      throw new CaptchaException();
    }
  }

  /**
   * Validate username and password inputs.
   *
   * @param username input username
   * @param password input password
   * @param sysUser SysUser object for uniqueness check
   * @return error message if validation fails, otherwise empty string
   */
  private String validateUserInput(String username, String password, SysUser sysUser) {
    if (StringUtils.isEmpty(username)) {
      return "Username cannot be empty.";
    }
    if (StringUtils.isEmpty(password)) {
      return "Password cannot be empty.";
    }
    if (username.length() < UserConstants.USERNAME_MIN_LENGTH
        || username.length() > UserConstants.USERNAME_MAX_LENGTH) {
      return "Username length must be between 2 and 20 characters.";
    }
    if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
        || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
      return "Password length must be between 5 and 20 characters.";
    }
    if (userRepository.existsByUserName(sysUser.getUserName())) {
      return "Registration failed. Username '" + username + "' already exists.";
    }
    return "";
  }
}
