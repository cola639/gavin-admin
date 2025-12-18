package com.api.system.service;

import com.api.common.domain.LoginUser;
import com.api.common.domain.SysUser;
import com.api.common.utils.MessageUtils;
import com.api.common.utils.StringUtils;
import com.api.framework.exception.ServiceException;
import com.api.framework.service.SysPasswordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom implementation of Spring Security's {@link UserDetailsService}.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Load user details from the database.
 *   <li>Validate account status (active, deleted, disabled).
 *   <li>Delegate password validation.
 *   <li>Assemble a {@link LoginUser} with roles and permissions.
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  private final SysUserService userService;
  private final SysPasswordService passwordService;
  private final SysPermissionService permissionService;

  /**
   * Load user details by username.
   *
   * @param username username provided during login
   * @return {@link UserDetails} for Spring Security authentication
   * @throws UsernameNotFoundException if user not found or invalid
   */
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    SysUser user = userService.selectUserByUserName(username);

    if (StringUtils.isNull(user)) {
      log.warn("Login attempt with non-existent user: {}", username);
      throw new ServiceException(MessageUtils.message("user.not.exists"));
    }
    //
    //    if (UserStatus.DELETED.getCode().equals(user.getDelFlag())) {
    //      log.warn("Login attempt with deleted user: {}", username);
    //      throw new ServiceException(MessageUtils.message("user.password.delete"));
    //    }
    //
    //    if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
    //      log.warn("Login attempt with disabled user: {}", username);
    //      throw new ServiceException(MessageUtils.message("user.blocked"));
    //    }

    // Validate password policy or expiration
    passwordService.validate(user);

    return createLoginUser(user);
  }

  /**
   * Assemble {@link LoginUser} with roles and permissions.
   *
   * @param user SysUser entity
   * @return authenticated LoginUser
   */
  private UserDetails createLoginUser(SysUser user) {
    return LoginUser.builder()
        .userId(user.getUserId())
        .deptId(user.getDeptId())
        .user(user)
        .permissions(permissionService.getMenuPermission(user))
        .build();
  }
}
