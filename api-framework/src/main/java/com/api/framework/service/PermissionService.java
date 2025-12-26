package com.api.framework.service;

import com.api.common.constant.Constants;
import com.api.common.domain.LoginUser;
import com.api.common.domain.SysRole;
import com.api.common.utils.SecurityUtils;
import com.api.common.utils.StringUtils;
import com.api.framework.security.context.PermissionContextHolder;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * Custom permission evaluation service.
 *
 * <p>This bean is usually referenced in SpEL like: {@code @ss.hasPermi('system:user:list')}.
 */
@Service("ss")
public class PermissionService {

  /**
   * Check whether the current user has a specific permission.
   *
   * @param permission permission string (e.g. "system:user:list")
   * @return true if the user has the permission, otherwise false
   */
  public boolean hasPermi(String permission) {
    if (StringUtils.isEmpty(permission)) {
      return false;
    }

    LoginUser loginUser = SecurityUtils.getLoginUser();
    if (loginUser == null || CollectionUtils.isEmpty(loginUser.getPermissions())) {
      return false;
    }

    PermissionContextHolder.setContext(permission);
    return hasPermission(loginUser.getPermissions(), permission);
  }

  /**
   * Check whether the current user does NOT have a specific permission.
   *
   * @param permission permission string
   * @return true if the user lacks the permission, otherwise false
   */
  public boolean lacksPermi(String permission) {
    return !hasPermi(permission);
  }

  /**
   * Check whether the current user has ANY permission in the provided list.
   *
   * <p>The input supports multiple permissions separated by {@link Constants#PERMISSION_DELIMITER}.
   *
   * @param permissions permission list string (e.g. "a:b:c,a:b:d")
   * @return true if the user has any of them, otherwise false
   */
  public boolean hasAnyPermi(String permissions) {
    if (StringUtils.isEmpty(permissions)) {
      return false;
    }

    LoginUser loginUser = SecurityUtils.getLoginUser();
    if (loginUser == null || CollectionUtils.isEmpty(loginUser.getPermissions())) {
      return false;
    }

    PermissionContextHolder.setContext(permissions);

    Set<String> userPermissions = loginUser.getPermissions();
    String[] items = permissions.split(Constants.PERMISSION_DELIMITER);

    for (String item : items) {
      if (item == null) {
        continue;
      }
      String perm = StringUtils.trim(item);
      if (StringUtils.isEmpty(perm)) {
        continue;
      }
      if (hasPermission(userPermissions, perm)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Check whether the current user has a specific role.
   *
   * <p>SUPER_ADMIN always passes.
   *
   * @param role role key (e.g. "admin")
   * @return true if the user has the role, otherwise false
   */
  public boolean hasRole(String role) {
    if (StringUtils.isEmpty(role)) {
      return false;
    }

    LoginUser loginUser = SecurityUtils.getLoginUser();
    if (loginUser == null
        || loginUser.getUser() == null
        || CollectionUtils.isEmpty(loginUser.getUser().getRoles())) {
      return false;
    }

    String expectedRole = StringUtils.trim(role);

    for (SysRole sysRole : loginUser.getUser().getRoles()) {
      if (sysRole == null) {
        continue;
      }
      String roleKey = sysRole.getRoleKey();
      if (StringUtils.isEmpty(roleKey)) {
        continue;
      }
      if (Constants.SUPER_ADMIN.equals(roleKey) || roleKey.equals(expectedRole)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Check whether the current user does NOT have a specific role.
   *
   * @param role role key
   * @return true if the user lacks the role, otherwise false
   */
  public boolean lacksRole(String role) {
    return !hasRole(role);
  }

  /**
   * Check whether the current user has ANY role in the provided list.
   *
   * <p>The input supports multiple roles separated by {@link Constants#ROLE_DELIMITER}.
   *
   * @param roles role list string (e.g. "admin,common")
   * @return true if the user has any of them, otherwise false
   */
  public boolean hasAnyRoles(String roles) {
    if (StringUtils.isEmpty(roles)) {
      return false;
    }

    LoginUser loginUser = SecurityUtils.getLoginUser();
    if (loginUser == null
        || loginUser.getUser() == null
        || CollectionUtils.isEmpty(loginUser.getUser().getRoles())) {
      return false;
    }

    String[] items = roles.split(Constants.ROLE_DELIMITER);

    for (String item : items) {
      if (item == null) {
        continue;
      }
      String role = StringUtils.trim(item);
      if (StringUtils.isEmpty(role)) {
        continue;
      }
      if (hasRole(role)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Low-level permission match.
   *
   * <p>Rules:
   *
   * <ul>
   *   <li>If the user has {@link Constants#ALL_PERMISSION}, then allow.
   *   <li>Otherwise, match the trimmed permission string.
   * </ul>
   */
  private boolean hasPermission(Set<String> permissions, String permission) {
    if (CollectionUtils.isEmpty(permissions) || StringUtils.isEmpty(permission)) {
      return false;
    }
    return permissions.contains(Constants.ALL_PERMISSION)
        || permissions.contains(StringUtils.trim(permission));
  }
}
