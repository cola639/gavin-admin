package com.api.persistence.domain.system;

import com.api.common.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * System Configuration Entity (sys_config)
 *
 * <p>Stores key-value pairs for configurable system settings.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Accessors(chain = true)
@Entity
@Table(name = "sys_config")
public class SysConfig extends BaseEntity implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /** Primary key (auto-increment) */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Comment("Primary key ID")
  private Long configId;

  /** Config name */
  @NotBlank(message = "Configuration name cannot be blank")
  @Size(max = 100, message = "Configuration name cannot exceed 100 characters")
  @Column(name = "config_name", nullable = false, length = 100)
  @Comment("Configuration name")
  private String configName;

  /** Config key */
  @NotBlank(message = "Configuration key cannot be blank")
  @Size(max = 100, message = "Configuration key cannot exceed 100 characters")
  @Column(name = "config_key", nullable = false, length = 100, unique = true)
  @Comment("Configuration key")
  private String configKey;

  /** Config value */
  @NotBlank(message = "Configuration value cannot be blank")
  @Size(max = 500, message = "Configuration value cannot exceed 500 characters")
  @Column(name = "config_value", nullable = false, length = 500)
  @Comment("Configuration value")
  private String configValue;

  /** Built-in flag (Y/N) */
  @Column(name = "config_type", length = 1)
  @Comment("System built-in flag (Y=Yes, N=No)")
  private String configType;
}
