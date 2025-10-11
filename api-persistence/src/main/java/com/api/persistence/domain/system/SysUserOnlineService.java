package com.api.persistence.domain.system;

import com.api.common.domain.LoginUser;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service implementation for managing online users.
 *
 * <p>Responsibilities: - Convert LoginUser sessions into SysUserOnline representations. - Filter
 * and search online users by IP address or username.
 *
 * <p>Design principles: - Uses @Slf4j for structured logging. - Uses constructor injection
 * with @Autowired. - Clean, null-safe, and easy to maintain. - Compatible with Java 17 and Spring
 * Boot 3.5.
 */
@Slf4j
@Service
public class SysUserOnlineService {

  /**
   * Finds an online user by IP address.
   *
   * @param ipaddr The login IP address to search for.
   * @param user The current login user session.
   * @return Online user information if matched, otherwise null.
   */
  public SysUserOnline selectOnlineByIpaddr(String ipaddr, LoginUser user) {
    if (user == null || StringUtils.isEmpty(ipaddr)) {
      log.debug("selectOnlineByIpaddr: skipped due to null input.");
      return null;
    }

    if (ipaddr.equals(user.getIpaddr())) {
      log.trace("Matched online user by IP: {}", ipaddr);
      return loginUserToUserOnline(user);
    }
    return null;
  }

  /**
   * Finds an online user by username.
   *
   * @param userName The username to search for.
   * @param user The current login user session.
   * @return Online user information if matched, otherwise null.
   */
  public SysUserOnline selectOnlineByUserName(String userName, LoginUser user) {
    if (user == null || StringUtils.isEmpty(userName)) {
      return null;
    }

    if (userName.equals(user.getUsername())) {
      log.trace("Matched online user by username: {}", userName);
      return loginUserToUserOnline(user);
    }
    return null;
  }

  /**
   * Finds an online user by both IP address and username.
   *
   * @param ipaddr The login IP address.
   * @param userName The username.
   * @param user The current login user session.
   * @return Online user information if both match, otherwise null.
   */
  public SysUserOnline selectOnlineByInfo(String ipaddr, String userName, LoginUser user) {
    if (user == null || StringUtils.isEmpty(ipaddr) || StringUtils.isEmpty(userName)) {
      return null;
    }

    if (ipaddr.equals(user.getIpaddr()) && userName.equals(user.getUsername())) {
      log.trace("Matched online user by IP [{}] and username [{}]", ipaddr, userName);
      return loginUserToUserOnline(user);
    }
    return null;
  }

  /**
   * Converts a LoginUser session into a SysUserOnline object.
   *
   * @param user The LoginUser instance from Redis or current session.
   * @return The corresponding SysUserOnline object or null if invalid.
   */
  public SysUserOnline loginUserToUserOnline(LoginUser user) {
    if (user == null || user.getUser() == null) {
      log.warn("Attempted to convert null LoginUser to SysUserOnline.");
      return null;
    }

    SysUserOnline online = new SysUserOnline();
    online.setTokenId(user.getToken());
    online.setUserName(user.getUsername());
    online.setIpaddr(user.getIpaddr());
    online.setLoginLocation(user.getLoginLocation());
    online.setBrowser(user.getBrowser());
    online.setOs(user.getOs());
    online.setLoginTime(user.getLoginTime());

    if (user.getUser().getDept() != null) {
      online.setDeptName(user.getUser().getDept().getDeptName());
    }

    log.debug(
        "Converted LoginUser [{}] to SysUserOnline [{}]", user.getUsername(), online.getIpaddr());
    return online;
  }
}
