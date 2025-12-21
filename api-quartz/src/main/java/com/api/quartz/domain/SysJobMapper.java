package com.api.quartz.domain;

import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SysJobMapper {

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mappings({
    @Mapping(target = "jobId", ignore = true),
    @Mapping(target = "createBy", ignore = true),
    @Mapping(target = "createTime", ignore = true),
    @Mapping(target = "nextValidTime", ignore = true) // @Transient getter
  })
  void updateNonNull(SysJob source, @MappingTarget SysJob target);
}
