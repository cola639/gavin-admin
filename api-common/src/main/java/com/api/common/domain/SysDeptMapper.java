package com.api.common.domain;

import org.mapstruct.*;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SysDeptMapper {

  /**
   * Copy updatable fields from request -> entity. - Ignore nulls (PATCH-like behavior) - Protect
   * system/relationship fields
   */
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mappings({
    @Mapping(target = "deptId", ignore = true),
    @Mapping(target = "ancestors", ignore = true),
    @Mapping(target = "parent", ignore = true),
    @Mapping(target = "children", ignore = true),

    // usually you don't allow changing delFlag via "update"
    @Mapping(target = "delFlag", ignore = true),

    // BaseEntity audit fields (adjust names to your BaseEntity)
    @Mapping(target = "createBy", ignore = true),
    @Mapping(target = "createTime", ignore = true),
    @Mapping(target = "updateBy", ignore = true),
    @Mapping(target = "updateTime", ignore = true),
    @Mapping(target = "remark", ignore = true),
  })
  void updateFromReq(SysDept req, @MappingTarget SysDept entity);
}
