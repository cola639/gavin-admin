package com.api.framework.service;

import com.api.common.constant.CacheConstants;
import com.api.common.constant.Constants;
import com.api.common.domain.LoginUser;
import com.api.common.redis.RedisCache;
import com.api.common.utils.ServletUtils;
import com.api.common.utils.StringUtils;
import com.api.common.utils.ip.AddressUtils;
import com.api.common.utils.ip.IpUtils;
import com.api.common.utils.uuid.IdUtils;
import eu.bitwalker.useragentutils.UserAgent;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Token handling service.
 *
 * <p>Responsibilities: - Create and parse JWT tokens - Store login users in Redis with expiration -
 * Refresh token expiration automatically - Track user agent, IP address, and location
 *
 * @author
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenService {

  private final RedisCache redisCache;

  /** Token header key */
  @Value("${token.header}")
  private String header;

  /** Secret key for signing JWT */
  @Value("${token.secret}")
  private String secret;

  /** Expiration time in minutes (default 30 min) */
  @Value("${token.expireTime}")
  private int expireTime;

  private static final long MILLIS_SECOND = 1000;
  private static final long MILLIS_MINUTE = 60 * MILLIS_SECOND;
  private static final long REFRESH_THRESHOLD = 20 * 60 * 1000L; // 20 min

  public LoginUser getLoginUser() {
    ServletRequestAttributes attrs =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attrs == null) return null;
    return getLoginUser(attrs.getRequest());
  }

  /** Retrieve user info from request token. */
  public LoginUser getLoginUser(HttpServletRequest request) {
    String token = getToken(request);
    if (StringUtils.isNotEmpty(token)) {
      try {
        Claims claims = parseToken(token);
        String uuid = claims.get(Constants.LOGIN_USER_KEY, String.class);
        String userKey = getTokenKey(uuid);
        return redisCache.getCacheObject(userKey, LoginUser.class);
      } catch (Exception e) {
        log.error("Failed to retrieve login user from token: {}", e.getMessage());
      }
    }
    return null;
  }

  /** Cache user info if token exists. */
  public void setLoginUser(LoginUser loginUser) {
    if (loginUser != null && StringUtils.isNotEmpty(loginUser.getToken())) {
      refreshToken(loginUser);
    }
  }

  /** Remove user info from Redis by token. */
  public void delLoginUser(String token) {
    if (StringUtils.isNotEmpty(token)) {
      redisCache.deleteObject(getTokenKey(token));
    }
  }

  /** Create a new JWT token and cache user in Redis. */
  public String createToken(LoginUser loginUser) {
    String token = IdUtils.fastUUID();
    loginUser.setToken(token);
    setUserAgent(loginUser);
    refreshToken(loginUser);

    Map<String, Object> claims = new HashMap<>();
    claims.put(Constants.LOGIN_USER_KEY, token);
    claims.put(Constants.JWT_USERNAME, loginUser.getUsername());

    return createToken(claims);
  }

  /**
   * Generate JWT from claims.
   *
   * @param claims Claims to include in the token
   * @return Generated JWT string
   */
  private String createToken(Map<String, Object> claims) {
    String token =
        Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.HS512, secret).compact();
    return token;
  }

  /** Verify token expiration. Refresh if less than 20 minutes left. */
  public void verifyToken(LoginUser loginUser) {
    long expireTime = loginUser.getExpireTime();
    long currentTime = System.currentTimeMillis();
    if (expireTime - currentTime <= REFRESH_THRESHOLD) {
      refreshToken(loginUser);
    }
  }

  /** Refresh token expiration and re-cache user. */
  public void refreshToken(LoginUser loginUser) {
    loginUser.setLoginTime(System.currentTimeMillis());
    loginUser.setExpireTime(loginUser.getLoginTime() + expireTime * MILLIS_MINUTE);
    redisCache.setCacheObject(
        getTokenKey(loginUser.getToken()), loginUser, expireTime, TimeUnit.MINUTES);
  }

  /** Capture client environment info (browser, OS, IP). */
  private void setUserAgent(LoginUser loginUser) {
    HttpServletRequest request = ServletUtils.getRequest();
    UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
    String ip = IpUtils.getIpAddr();

    loginUser.setIpaddr(ip);
    loginUser.setLoginLocation(AddressUtils.getRealAddressByIP(ip));
    loginUser.setBrowser(userAgent.getBrowser().getName());
    loginUser.setOs(userAgent.getOperatingSystem().getName());
  }

  /** Parse token into claims. */
  private Claims parseToken(String token) {
    return Jwts.parserBuilder().setSigningKey(secret).build().parseClaimsJws(token).getBody();
  }

  /** Get username from token. */
  public String getUsernameFromToken(String token) {
    return parseToken(token).getSubject();
  }

  /** Extract raw token string from request header. */
  private String getToken(HttpServletRequest request) {
    String token = request.getHeader(header);
    if (StringUtils.isNotEmpty(token) && token.startsWith(Constants.TOKEN_PREFIX)) {
      return token.replace(Constants.TOKEN_PREFIX, "");
    }
    return token;
  }

  /** Build Redis cache key for token. */
  private String getTokenKey(String uuid) {
    return CacheConstants.LOGIN_TOKEN_KEY + uuid;
  }

  /**
   * Extracts username from the current HTTP request's JWT token.
   *
   * @param request the current HTTP request
   * @return username if token is valid, otherwise null
   */
  public String extractUsername(HttpServletRequest request) {
    try {
      String token = getToken(request);
      if (StringUtils.isNotEmpty(token)) {
        Claims claims = parseToken(token);
        return claims.get(Constants.JWT_USERNAME, String.class);
      }
    } catch (Exception e) {
      log.error("❌ Failed to extract username from token: {}", e.getMessage());
    }
    return null;
  }
}
