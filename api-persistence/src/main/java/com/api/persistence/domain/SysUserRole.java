package com.api.persistence.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * User and Role association entity. This entity represents the mapping between users and roles in
 * the system.
 */
@Entity
@Table(name = "sys_user_role")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@IdClass(SysUserRole.SysUserRoleId.class) // 指定内部类作为复合主键
public class SysUserRole {
  /** Composite primary key for SysUserRole. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SysUserRoleId implements Serializable {
    private Long userId;
    private Long roleId;
  }

  @Id
  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Id
  @Column(name = "role_id", nullable = false)
  private Long roleId;
}
