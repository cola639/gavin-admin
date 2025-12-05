package com.api.common.domain;

import com.api.common.domain.SysUser;
import com.api.common.domain.SysUserDTO;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SysUserMapper {

  /** Copy non-null fields from update request into existing entity. */
  void updateFromDto(SysUserDTO dto, @MappingTarget SysUser entity);
}
