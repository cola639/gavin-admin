package com.api.persistence.domain.system;

import com.api.common.domain.SysDept;
import com.api.common.domain.SysRole;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

/** Role-Department association entity (sys_role_dept) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "sys_role_dept")
@IdClass(SysRoleDept.SysRoleDeptId.class)
public class SysRoleDept implements Serializable {
  /** Composite primary key for SysUserRole. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SysRoleDeptId implements Serializable {
    private Long roleId;
    private Long deptId;
  }

  @Id
  @Column(name = "role_id")
  private Long roleId;

  @Id
  @Column(name = "dept_id")
  private Long deptId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "role_id", insertable = false, updatable = false)
  private SysRole role;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "dept_id", insertable = false, updatable = false)
  private SysDept dept;
}
