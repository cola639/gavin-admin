package com.api.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Enable JPA auditing. */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
  // No content needed. Only enable auditing.
}
