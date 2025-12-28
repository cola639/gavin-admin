package com.api.common.domain;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for SysRole.
 *
 * <p>Used to copy updatable fields from a request object into an existing entity: - Ignore nulls
 * (PATCH-like behavior) - Protect key/system fields and audit fields
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SysRoleMapper {

  /**
   * Copy updatable fields from req -> entity.
   *
   * <p>Notes: - roleId is immutable - delFlag is usually controlled by system (soft delete), not by
   * update APIs - transient/UI helper fields should never be persisted via update mapping - audit
   * fields are managed by BaseEntity / persistence layer
   */
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mappings({
    // Primary key
    @Mapping(target = "roleId", ignore = true),

    // Soft delete flag should not be updated by normal update APIs
    @Mapping(target = "delFlag", ignore = true),

    // Transient/UI helper fields (not persisted)
    @Mapping(target = "flag", ignore = true),
    @Mapping(target = "menuIds", ignore = true),
    @Mapping(target = "deptIds", ignore = true),
    @Mapping(target = "permissions", ignore = true),

    // BaseEntity audit fields (adjust names if your BaseEntity differs)
    @Mapping(target = "createBy", ignore = true),
    @Mapping(target = "createTime", ignore = true),
    @Mapping(target = "updateBy", ignore = true),
    @Mapping(target = "updateTime", ignore = true),
    @Mapping(target = "remark", ignore = true),
  })
  void updateFromReq(SysRole req, @MappingTarget SysRole entity);
}
