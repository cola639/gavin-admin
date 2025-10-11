package com.api.system.service;

import com.api.common.constant.UserConstants;
import com.api.common.domain.SysRole;
import com.api.common.domain.SysUser;
import com.api.common.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service for handling user role and menu permissions.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Resolve role-based permissions for a given user.
 *   <li>Resolve menu-based permissions (authorities) for a given user.
 *   <li>Special handling for administrators (all roles and permissions).
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysPermissionService {

  private final SysRoleService roleService;
  private final SysMenuService menuService;

  /**
   * Retrieve role permissions for a user.
   *
   * @param user the system user
   * @return set of role keys
   */
  public Set<String> getRolePermission(SysUser user) {
    Set<String> roles = new HashSet<>();
    if (user.isAdmin()) {
      roles.add("admin");
      log.debug("User [{}] is admin, granted all roles.", user.getUserName());
    } else {
      roles.addAll(roleService.selectRolePermissionByUserId(user.getUserId()));
    }
    return roles;
  }

  /**
   * Retrieve menu (functional) permissions for a user.
   *
   * @param user the system user
   * @return set of permission strings
   */
  public Set<String> getMenuPermission(SysUser user) {
    Set<String> perms = new HashSet<>();

    if (user.isAdmin()) {
      perms.add("*:*:*"); // Full access wildcard
      log.debug("User [{}] is admin, granted all permissions.", user.getUserName());
      return perms;
    }

    List<SysRole> roles = user.getRoles();
    if (!CollectionUtils.isEmpty(roles)) {
      for (SysRole role : roles) {
        if (StringUtils.equals(role.getStatus(), UserConstants.ROLE_NORMAL) && !role.isAdmin()) {
          Set<String> rolePerms = menuService.selectMenuPermsByRoleId(role.getRoleId());
          role.setPermissions(rolePerms); // assign perms back to role for reference
          perms.addAll(rolePerms);
        }
      }
    } else {
      perms.addAll(menuService.selectMenuPermsByUserId(user.getUserId()));
    }

    return perms;
  }
}
