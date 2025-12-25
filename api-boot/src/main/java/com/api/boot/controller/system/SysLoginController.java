package com.api.boot.controller.system;

import com.api.common.constant.Constants;

import com.api.common.domain.AjaxResult;
import com.api.common.domain.LoginBody;
import com.api.common.domain.LoginUser;
import com.api.common.domain.SysUser;
import com.api.common.redis.RedisCache;
import com.api.common.utils.SecurityUtils;
import com.api.common.domain.SysMenu;
import com.api.system.repository.SysUserRepository;
import com.api.system.service.SysLoginService;
import com.api.system.service.SysPermissionService;
import com.api.framework.service.TokenService;

import com.api.system.service.SysMenuService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * REST controller for login and authentication endpoints.
 *
 * <p>Provides: - User login with JWT token generation - Fetching authenticated user information
 * (roles, permissions) - Retrieving dynamic router/menu data
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class SysLoginController {

  private final SysLoginService loginService;
  private final SysMenuService menuService;
  private final SysPermissionService permissionService;
  private final TokenService tokenService;
  private final RedisCache redisCache;
  private final SysUserRepository userRepository;

  @Autowired private ObjectMapper objectMapper;

  /**
   * Login endpoint.
   *
   * @param loginBody login request containing username, password, captcha, uuid
   * @return token response
   */
  @PostMapping("/login")
  public AjaxResult login(@RequestBody LoginBody loginBody) {
    log.info("Login attempt for user: {}", loginBody.getEmail());

    String token =
        loginService.login(
            loginBody.getEmail(),
            loginBody.getPassword(),
            loginBody.getCode(),
            loginBody.getUuid());

    AjaxResult ajax = AjaxResult.success();
    ajax.put(Constants.TOKEN, token);
    return ajax;
  }

  /**
   * Get user info endpoint.
   *
   * @return user info including roles and permissions
   */
  @GetMapping("getInfo")
  public AjaxResult getInfo() {
    LoginUser loginUser = SecurityUtils.getLoginUser();
    SysUser user = loginUser.getUser();

    Set<String> roles = permissionService.getRolePermission(user);
    Set<String> permissions = permissionService.getMenuPermission(user);
    if (!loginUser.getPermissions().equals(permissions)) {
      loginUser.setPermissions(permissions);
      tokenService.refreshToken(loginUser);
    }
    AjaxResult ajax = AjaxResult.success();
    ajax.put("user", user);
    ajax.put("roles", roles);
    ajax.put("permissions", permissions);
    return ajax;
  }

  /** Get routers endpoint. */
  @GetMapping("getRouters")
  public AjaxResult getRouters() {
    Long userId = SecurityUtils.getUserId();
    List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId);
    return AjaxResult.success(menuService.buildMenus(menus));
  }

  @PostMapping("/testToken")
  public AjaxResult testLogin() {

    AjaxResult ajax = AjaxResult.success();
    return ajax;
  }

  @GetMapping("/write")
  public AjaxResult testWrite() throws JsonProcessingException {
    SysUser user = userRepository.findById(1L).orElseThrow();

    // Serialize to JSON string
    String json = objectMapper.writeValueAsString(user);
    redisCache.setCacheObject("user:" + user.getUserId(), user);
    return AjaxResult.success();
  }

  @GetMapping("/read")
  public AjaxResult testGet() throws JsonProcessingException {
    // Get JSON string from Redis
    //    String json = redisCache.getCacheObject("user:1");

    // Deserialize back into SysUser
    //    SysUser user = objectMapper.readValue(json, SysUser.class);

    return AjaxResult.success();
  }

  /**
   * Get information about the currently logged-in user.
   *
   * @return user info including roles, permissions, password expiration status
   */
  //  @GetMapping("/info")
  //  public AjaxResult getInfo() {
  //    LoginUser loginUser = SecurityUtils.getLoginUser();
  //    SysUser user = loginUser.getUser();
  //
  //    // Gather roles and permissions
  //    Set<String> roles = permissionService.getRolePermission(user);
  //    Set<String> permissions = permissionService.getMenuPermission(user);
  //
  //    // Refresh token if permissions have changed
  //    if (!loginUser.getPermissions().equals(permissions)) {
  //      loginUser.setPermissions(permissions);
  //      tokenService.refreshToken(loginUser);
  //    }
  //
  //    // Build response
  //    return AjaxResult.success()
  //        .put("user", user)
  //        .put("roles", roles)
  //        .put("permissions", permissions)
  //        .put("isDefaultModifyPwd", isInitPasswordUnchanged(user.getPwdUpdateDate()))
  //        .put("isPasswordExpired", isPasswordExpired(user.getPwdUpdateDate()));
  //  }

  /**
   * Get dynamic router configuration for the current user.
   *
   * @return router/menu tree
   */
  //  @GetMapping("/routers")
  //  public AjaxResult getRouters() {
  //    Long userId = SecurityUtils.getUserId();
  //    List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId);
  //    return AjaxResult.success(menuService.buildMenus(menus));
  //  }
  //
  //  /** Check if the user is still using the initial system password. */
  //  private boolean isInitPasswordUnchanged(Date pwdUpdateDate) {
  //    Integer initPasswordModify =
  //        Convert.toInt(configService.selectConfigByKey("sys.account.initPasswordModify"));
  //    return initPasswordModify != null && initPasswordModify == 1 && pwdUpdateDate == null;
  //  }

  /** Check if the user's password is expired based on configured validity days. */
  //  private boolean isPasswordExpired(Date pwdUpdateDate) {
  //    Integer passwordValidateDays =
  //        Convert.toInt(configService.selectConfigByKey("sys.account.passwordValidateDays"));
  //
  //    if (passwordValidateDays != null && passwordValidateDays > 0) {
  //      if (StringUtils.isNull(pwdUpdateDate)) {
  //        return true; // never updated -> expired
  //      }
  //      return DateUtils.differentDaysByMillisecond(DateUtils.getNowDate(), pwdUpdateDate)
  //          > passwordValidateDays;
  //    }
  //    return false;
  //  }
}
