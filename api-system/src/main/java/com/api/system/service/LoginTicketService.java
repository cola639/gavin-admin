package com.api.system.service;

import com.api.common.redis.RedisCache;
import com.api.common.utils.uuid.IdUtils;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginTicketService {

  private static final String KEY_PREFIX = "login:ticket:";
  private static final int TTL_SECONDES = 60;

  private final RedisCache redisCache;

  public String issue(String jwt) {
    String code = IdUtils.fastUUID();
    String key = KEY_PREFIX + code;
    redisCache.setCacheObject(key, jwt, TTL_SECONDES, TimeUnit.SECONDS); // stored as raw String
    log.info("Issued login ticket code={}, TTL_SECONDES={}", code, TTL_SECONDES);
    return code;
  }

  /** Single-use exchange. */
  public String consume(String code) {
    if (code == null || code.isBlank()) {
      return null;
    }
    String key = KEY_PREFIX + code;
    String jwt = redisCache.getCacheObject(key, String.class);
    if (jwt == null || jwt.isBlank()) {
      return null;
    }
    redisCache.deleteObject(key);
    log.info("Consumed login ticket code={}", code);
    return jwt;
  }
}
