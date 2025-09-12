package com.api.common.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Base entity class for all persistent objects.
 * <p>
 * Provides common audit fields and request-related metadata
 * that are automatically inherited by all entities in the system.
 * </p>
 *
 * Features:
 * - Audit tracking (createdBy, createdTime, updatedBy, updatedTime)
 * - Search support
 * - Additional request parameters
 * - JSON serialization formatting and control
 *
 * Lombok annotations are used to reduce boilerplate code.
 *
 * Author: ruoyi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Search value (used in filtering/query conditions, not persisted) */
    @JsonIgnore
    private String searchValue;

    /** User who created the record */
    private String createBy;

    /** Creation timestamp */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** User who last updated the record */
    private String updateBy;

    /** Last updated timestamp */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** Remarks or additional notes */
    private String remark;

    /** Extra request parameters (useful for dynamic queries) */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Builder.Default
    private Map<String, Object> params = new HashMap<>();

}
