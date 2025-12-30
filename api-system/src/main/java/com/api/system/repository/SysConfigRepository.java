package com.api.system.repository;

import com.api.system.domain.system.SysConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** Repository for system configuration persistence. */
@Repository
public interface SysConfigRepository extends JpaRepository<SysConfig, Long> {

  Optional<SysConfig> findByConfigKey(String configKey);

  boolean existsByConfigKey(String configKey);
}
