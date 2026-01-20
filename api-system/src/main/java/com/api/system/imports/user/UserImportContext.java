package com.api.system.imports.user;

import com.api.common.domain.SysUser;

/** Validation context for user import rows. */
public class UserImportContext {

  private final SysUser existingUser;
  private final Long deptId;

  public UserImportContext(SysUser existingUser, Long deptId) {
    this.existingUser = existingUser;
    this.deptId = deptId;
  }

  public SysUser getExistingUser() {
    return existingUser;
  }

  public Long getDeptId() {
    return deptId;
  }
}
