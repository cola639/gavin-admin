package com.api.boot.controller.system;

import com.api.common.constant.Constants;

import com.api.common.domain.AjaxResult;
import com.api.common.utils.SecurityUtils;
import com.api.common.utils.StringUtils;
import com.api.framework.service.SysLoginService;
import com.api.framework.service.SysPermissionService;
import com.api.framework.service.TokenService;

import com.api.persistence.domain.common.LoginBody;
import com.api.system.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
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

  /**
   * Login endpoint.
   *
   * @param loginBody login request containing username, password, captcha, uuid
   * @return token response
   */
  @PostMapping("/login")
  public AjaxResult login(@RequestBody LoginBody loginBody) {
    log.info("Login attempt for user: {}", loginBody.getUsername());
    String token =
        loginService.login(
            loginBody.getUsername(),
            loginBody.getPassword(),
            loginBody.getCode(),
            loginBody.getUuid());

    AjaxResult ajax = AjaxResult.success();
    ajax.put(Constants.TOKEN, token);
    return ajax;
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
