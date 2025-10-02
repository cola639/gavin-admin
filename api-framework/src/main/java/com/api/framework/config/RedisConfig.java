package com.api.framework.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis configuration class.
 *
 * <p>- Configures RedisTemplate with String keys and Jackson-based JSON serialization for values.
 *
 * <p>- Provides a Lua script bean for request rate limiting.
 *
 * @author
 */
@Slf4j
@Configuration
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport {

  /**
   * Configure RedisTemplate with String keys and JSON-serialized values using
   * GenericJackson2JsonRedisSerializer (recommended for Spring Data Redis 3.x+).
   *
   * @param connectionFactory Redis connection factory
   * @return RedisTemplate configured with serializers
   */
  @Bean
  public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<Object, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);

    // Key serialization with String
    template.setKeySerializer(new StringRedisSerializer());
    template.setHashKeySerializer(new StringRedisSerializer());

    // Value serialization with Jackson (supports polymorphic types)
    GenericJackson2JsonRedisSerializer jacksonSerializer = new GenericJackson2JsonRedisSerializer();
    template.setValueSerializer(jacksonSerializer);
    template.setHashValueSerializer(jacksonSerializer);

    template.afterPropertiesSet();
    log.info("RedisTemplate configured successfully with GenericJackson2JsonRedisSerializer.");
    return template;
  }

  /**
   * Lua script for rate-limiting (simple counter-based approach).
   *
   * @return RedisScript returning the current request count
   */
  @Bean
  public DefaultRedisScript<Long> limitScript() {
    DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
    redisScript.setScriptText(limitScriptText());
    redisScript.setResultType(Long.class);
    return redisScript;
  }

  /**
   * Lua script for request rate-limiting.
   *
   * <p>- Increments a counter for a given key. - Sets expiration if key is new. - Returns current
   * count.
   */
  private String limitScriptText() {
    return """
                local key = KEYS[1]
                local count = tonumber(ARGV[1])
                local time = tonumber(ARGV[2])
                local current = redis.call('get', key)
                if current and tonumber(current) > count then
                    return tonumber(current)
                end
                current = redis.call('incr', key)
                if tonumber(current) == 1 then
                    redis.call('expire', key, time)
                end
                return tonumber(current)
                """;
  }
}
