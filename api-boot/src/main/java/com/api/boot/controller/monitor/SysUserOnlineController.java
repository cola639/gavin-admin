package com.api.boot.controller.monitor;

import com.api.common.constant.CacheConstants;
import com.api.common.controller.BaseController;
import com.api.common.domain.AjaxResult;
import com.api.common.domain.LoginUser;
import com.api.common.redis.RedisCache;
import com.api.common.utils.StringUtils;
import com.api.common.utils.pagination.TableDataInfo;
import com.api.persistence.domain.system.SysUserOnline;
import com.api.persistence.domain.system.SysUserOnlineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/** REST controller for monitoring and managing online users. */
@Slf4j
@RestController
@RequestMapping("/monitor/online")
@RequiredArgsConstructor
public class SysUserOnlineController extends BaseController {

  /** Online user management service. */
  private final SysUserOnlineService userOnlineService;

  /** Redis cache for token/session data. */
  private final RedisCache redisCache;

  /**
   * Retrieves a paginated list of currently active (online) users.
   *
   * @param ipaddr Optional IP address filter
   * @param userName Optional username filter
   * @return Paginated list of online users
   */
  // @PreAuthorize("@ss.hasPermi('monitor:online:list')")
  @GetMapping("/list")
  public TableDataInfo list(
      @RequestParam(required = false) String ipaddr,
      @RequestParam(required = false) String userName) {

    log.info("Fetching online users with filters: ipaddr='{}', userName='{}'", ipaddr, userName);

    Collection<String> keys = redisCache.keys(CacheConstants.LOGIN_TOKEN_KEY + "*");
    List<SysUserOnline> userOnlineList = new ArrayList<>();

    for (String key : keys) {
      LoginUser user = redisCache.getCacheObject(key, LoginUser.class);
      if (user == null) {
        continue;
      }

      SysUserOnline onlineUser = null;

      if (StringUtils.isNotEmpty(ipaddr) && StringUtils.isNotEmpty(userName)) {
        onlineUser = userOnlineService.selectOnlineByInfo(ipaddr, userName, user);
      } else if (StringUtils.isNotEmpty(ipaddr)) {
        onlineUser = userOnlineService.selectOnlineByIpaddr(ipaddr, user);
      } else if (StringUtils.isNotEmpty(userName) && StringUtils.isNotNull(user.getUser())) {
        onlineUser = userOnlineService.selectOnlineByUserName(userName, user);
      } else {
        onlineUser = userOnlineService.loginUserToUserOnline(user);
      }

      if (onlineUser != null) {
        userOnlineList.add(onlineUser);
      }
    }

    Collections.reverse(userOnlineList);

    log.info("Found {} online users.", userOnlineList.size());
    return getDataTable(userOnlineList);
  }

  /**
   * Forces a user to log out (invalidates their token).
   *
   * @param tokenId The token identifier of the user to force logout
   * @return AjaxResult with success or failure response
   */
  // @PreAuthorize("@ss.hasPermi('monitor:online:forceLogout')")
  @DeleteMapping("/{tokenId}")
  public AjaxResult forceLogout(@PathVariable String tokenId) {
    String redisKey = CacheConstants.LOGIN_TOKEN_KEY + tokenId;

    log.warn("Forcing logout for token: {}", tokenId);

    boolean exists = Boolean.TRUE.equals(redisCache.deleteObject(redisKey));
    if (exists) {
      log.info("Successfully removed session for token: {}", tokenId);
      return success("User forcibly logged out successfully");
    } else {
      log.warn("No active session found for token: {}", tokenId);
      return error("User session not found or already logged out");
    }
  }
}
