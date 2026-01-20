package com.api.system.imports.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Raw row data mapped from Excel. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserImportRow {

  private String userName;
  private String nickName;
  private String email;
  private String phoneNumber;
  private String status;
  private String sex;
  private String userType;
  private String deptId;
  private String password;
  private String remark;
}
