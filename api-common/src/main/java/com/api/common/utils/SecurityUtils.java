package com.api.common.utils;

import com.api.common.constant.Constants;
import com.api.common.domain.LoginUser;
import com.api.common.domain.SysRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.PatternMatchUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Security utility class.
 *
 * <p>Provides helper methods for: - Getting current authenticated user info - Working with roles
 * and permissions - Encrypting and validating passwords
 */
@Slf4j
public final class SecurityUtils {

  private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

  // Prevent instantiation
  private SecurityUtils() {
    throw new UnsupportedOperationException("Utility class - do not instantiate");
  }

  /**
   * Get the current user's ID.
   *
   * @return user ID
   */
  public static Long getUserId() {
    try {
      return getLoginUser().getUserId();
    } catch (Exception e) {
      log.error("Failed to get user ID", e);
      throw new IllegalStateException("Unable to get user ID", e);
    }
  }

  /**
   * Get the current user's department ID.
   *
   * @return department ID
   */
  public static Long getDeptId() {
    try {
      return getLoginUser().getDeptId();
    } catch (Exception e) {
      log.error("Failed to get department ID", e);
      throw new IllegalStateException("Unable to get department ID", e);
    }
  }

  /**
   * Get the current username.
   *
   * @return username
   */
  public static String getUsername() {
    try {
      return getLoginUser().getUsername();
    } catch (Exception e) {
      log.error("Failed to get username", e);
      throw new IllegalStateException("Unable to get username", e);
    }
  }

  /**
   * Get the current logged-in user.
   *
   * @return LoginUser
   */
  public static LoginUser getLoginUser() {
    try {
      return (LoginUser) getAuthentication().getPrincipal();
    } catch (Exception e) {
      log.error("Failed to get LoginUser", e);
      throw new IllegalStateException("Unable to get LoginUser", e);
    }
  }

  /**
   * Get the current Authentication object.
   *
   * @return Authentication
   */
  public static Authentication getAuthentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  /**
   * Encrypt a password using BCrypt.
   *
   * @param password raw password
   * @return hashed password
   */
  public static String encryptPassword(String password) {
    return PASSWORD_ENCODER.encode(password);
  }

  /**
   * Validate if a raw password matches an encoded password.
   *
   * @param rawPassword plain text password
   * @param encodedPassword hashed password
   * @return true if matches
   */
  public static boolean matchesPassword(String rawPassword, String encodedPassword) {
    return PASSWORD_ENCODER.matches(rawPassword, encodedPassword);
  }

  /**
   * Check if a user is admin.
   *
   * @param userId user ID
   * @return true if admin
   */
  public static boolean isAdmin(Long userId) {
    return userId != null && userId == 1L;
  }

  /**
   * Check if the current user has a specific permission.
   *
   * @param permission permission string
   * @return true if user has permission
   */
  public static boolean hasPermission(String permission) {
    return hasPermission(getLoginUser().getPermissions(), permission);
  }

  /**
   * Check if a user has a specific permission.
   *
   * @param authorities user permissions
   * @param permission required permission
   * @return true if matched
   */
  public static boolean hasPermission(Collection<String> authorities, String permission) {
    return authorities.stream()
        .filter(StringUtils::hasText)
        .anyMatch(
            x ->
                Constants.ALL_PERMISSION.equals(x) || PatternMatchUtils.simpleMatch(x, permission));
  }

  /**
   * Check if the current user has a role.
   *
   * @param role role string
   * @return true if matched
   */
  public static boolean hasRole(String role) {
    List<SysRole> roles = getLoginUser().getUser().getRoles();
    Collection<String> roleKeys =
        roles.stream().map(SysRole::getRoleKey).collect(Collectors.toSet());
    return hasRole(roleKeys, role);
  }

  /**
   * Check if roles contain a specific role.
   *
   * @param roles role list
   * @param role required role
   * @return true if matched
   */
  public static boolean hasRole(Collection<String> roles, String role) {
    return roles.stream()
        .filter(StringUtils::hasText)
        .anyMatch(x -> Constants.SUPER_ADMIN.equals(x) || PatternMatchUtils.simpleMatch(x, role));
  }
}
