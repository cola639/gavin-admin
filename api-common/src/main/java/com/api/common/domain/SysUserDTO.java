package com.api.common.domain;

import lombok.Builder;
import java.util.Date;

@Builder
public record SysUserDTO(
    Long userId,
    String userName,
    String nickName,
    String email,
    String avatar,
    String phonenumber,
    String status,
    Date createTime,
    Long deptId,
    String deptName) {}
