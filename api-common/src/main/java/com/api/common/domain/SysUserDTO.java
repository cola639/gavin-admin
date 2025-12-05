package com.api.common.domain;

import com.api.common.annotation.Xls;
import com.api.common.domain.SysUser;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/** User DTO for list/query responses. Pure DTO: no JPA annotations, no persistence behavior. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SysUserDTO {

  private Long userId;

  @Xls(name = "User Name", order = 1)
  private String userName;

  @Xls(name = "Nick Name", order = 2)
  private String nickName;

  @Xls(name = "Email", order = 3)
  private String email;

  private String avatar;

  @Xls(name = "Phone Number", order = 4)
  private String phonenumber;

  @Xls(name = "Status", order = 5)
  private String status;

  @Xls(name = "Create Time", order = 6)
  private Date createTime;

  private Long deptId;

  @Xls(name = "Dept Name", order = 7)
  private String deptName;

  private Long[] roleIds;
  private Long[] postIds;

  // ----------------------------------------------------------------------
  // Static factory methods: Entity -> DTO
  // ----------------------------------------------------------------------

  /** Convert a SysUser entity to SysUserDTO. */
  public static SysUserDTO fromEntity(SysUser entity) {
    if (entity == null) {
      return null;
    }
    return SysUserDTO.builder()
        .userId(entity.getUserId())
        .userName(entity.getUserName())
        .nickName(entity.getNickName())
        .email(entity.getEmail())
        .avatar(entity.getAvatar())
        .phonenumber(entity.getPhonenumber())
        .status(entity.getStatus())
        .createTime(entity.getCreateTime())
        .deptId(entity.getDeptId())
        .deptName(entity.getDept() != null ? entity.getDept().getDeptName() : null)
        .build();
  }

  /** Convert a list of SysUser entities to a list of SysUserDTO. */
  public static List<SysUserDTO> fromEntities(List<SysUser> entities) {
    if (entities == null || entities.isEmpty()) {
      return Collections.emptyList();
    }
    return entities.stream().map(SysUserDTO::fromEntity).collect(Collectors.toList());
  }
}
