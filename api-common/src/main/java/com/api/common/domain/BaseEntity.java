package com.api.common.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base entity class for all persistent objects.
 *
 * <p>Provides common audit fields and request-related metadata that are automatically inherited by
 * all entities in the system.
 *
 * <p>Features: - Audit tracking (createdBy, createdTime, updatedBy, updatedTime) - Search support -
 * Additional request parameters - JSON serialization formatting and control
 *
 * <p>Lombok annotations are used to reduce boilerplate code.
 *
 * <p>Author: ruoyi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@MappedSuperclass
public class BaseEntity implements Serializable {

  @Column(name = "create_by")
  private String createBy;

  @Column(name = "create_time")
  private Date createTime;

  @Column(name = "update_by")
  private String updateBy;

  @Column(name = "update_time")
  private Date updateTime;

  @Column(name = "remark")
  private String remark;

  /** Used for searching/filtering only — not persisted. */
  @Transient // ✅ Tells JPA not to map this field to a column
  @JsonIgnore
  private String searchValue;

  /** Used for dynamic query parameters — not persisted. */
  @Transient // ✅ Same here
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @Builder.Default
  private Map<String, Object> params = new HashMap<>();
}
