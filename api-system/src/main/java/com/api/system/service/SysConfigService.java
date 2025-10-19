package com.api.system.service;

import com.api.common.constant.CacheConstants;
import com.api.common.constant.UserConstants;
import com.api.common.redis.RedisCache;
import com.api.common.utils.StringUtils;
import com.api.framework.exception.ServiceException;
import com.api.persistence.domain.system.SysConfig;
import com.api.persistence.repository.system.SysConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;

/** Implementation of system configuration service. */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysConfigService {

  private final SysConfigRepository configRepository;

  private final RedisCache redisCache;

  /** Initialize configuration cache after the app is fully started */
  @EventListener(ApplicationReadyEvent.class)
  public void initAfterStartup() {
    log.info("🚀 Application started. Initializing system configuration cache...");
    reloadCache();
  }

  public SysConfig findById(Long id) {
    return configRepository
        .findById(id)
        .orElseThrow(() -> new ServiceException("Configuration not found for ID: " + id));
  }

  public SysConfig findByKey(String key) {
    String cacheKey = getCacheKey(key);
    String cachedValue = redisCache.getCacheObject(cacheKey);

    if (StringUtils.isNotEmpty(cachedValue)) {
      return SysConfig.builder().configKey(key).configValue(cachedValue).build();
    }

    SysConfig config =
        configRepository
            .findByConfigKey(key)
            .orElseThrow(() -> new ServiceException("Configuration not found for key: " + key));

    redisCache.setCacheObject(cacheKey, config.getConfigValue());
    return config;
  }

  public List<SysConfig> findAll(SysConfig filter) {
    return configRepository.findAll(); // TODO: add dynamic filtering if needed
  }

  public SysConfig create(SysConfig config) {
    if (!isKeyUnique(config)) {
      throw new ServiceException("Configuration key already exists: " + config.getConfigKey());
    }

    SysConfig saved = configRepository.save(config);
    redisCache.setCacheObject(getCacheKey(saved.getConfigKey()), saved.getConfigValue());
    return saved;
  }

  public SysConfig update(SysConfig config) {
    SysConfig existing = findById(config.getConfigId());

    if (!existing.getConfigKey().equals(config.getConfigKey())) {
      redisCache.deleteObject(getCacheKey(existing.getConfigKey()));
    }

    SysConfig updated = configRepository.save(config);
    redisCache.setCacheObject(getCacheKey(updated.getConfigKey()), updated.getConfigValue());
    return updated;
  }

  public void deleteByIds(Long[] ids) {
    for (Long id : ids) {
      SysConfig config = findById(id);

      if (UserConstants.YES.equals(config.getConfigType())) {
        throw new ServiceException(
            "Built-in configuration cannot be deleted: " + config.getConfigKey());
      }

      configRepository.deleteById(id);
      redisCache.deleteObject(getCacheKey(config.getConfigKey()));
    }
  }

  public boolean isKeyUnique(SysConfig config) {
    return configRepository
        .findByConfigKey(config.getConfigKey())
        .map(existing -> existing.getConfigId().equals(config.getConfigId()))
        .orElse(true);
  }

  public void reloadCache() {
    try {
      log.info("♻️ Reloading system configuration cache...");
      clearCache();

      // Load all configurations from the database
      configRepository
          .findAll()
          .forEach(
              cfg ->
                  redisCache.setCacheObject(getCacheKey(cfg.getConfigKey()), cfg.getConfigValue()));
      log.info("✅ Configuration cache successfully loaded ({} entries)", configRepository.count());
    } catch (Exception e) {
      log.error("❌ Failed to initialize configuration cache", e);
    }
  }

  /** Clears the entire system configuration cache. */
  public void clearCache() {
    Collection<String> keys = redisCache.keys(CacheConstants.SYS_CONFIG_KEY + "*");
    redisCache.deleteObject(keys);
  }

  private String getCacheKey(String configKey) {
    return CacheConstants.SYS_CONFIG_KEY + configKey;
  }
}
