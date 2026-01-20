package com.api.system.imports.user;

import com.api.common.utils.StringUtils;
import com.api.common.utils.excel.ExcelRow;
import com.api.system.imports.base.ImportRowMapper;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Map Excel rows into user import rows without business rules. */
@Component
public class UserImportRowMapper implements ImportRowMapper<UserImportRow> {

  @Override
  public UserImportRow map(ExcelRow excelRow) {
    Map<String, String> values = excelRow.getValues();

    return UserImportRow.builder()
        .userName(StringUtils.trim(values.get(UserImportContract.COL_USER_NAME)))
        .nickName(StringUtils.trim(values.get(UserImportContract.COL_NICK_NAME)))
        .email(StringUtils.trim(values.get(UserImportContract.COL_EMAIL)))
        .phoneNumber(StringUtils.trim(values.get(UserImportContract.COL_PHONE_NUMBER)))
        .status(StringUtils.trim(values.get(UserImportContract.COL_STATUS)))
        .sex(StringUtils.trim(values.get(UserImportContract.COL_SEX)))
        .userType(StringUtils.trim(values.get(UserImportContract.COL_USER_TYPE)))
        .deptId(StringUtils.trim(values.get(UserImportContract.COL_DEPT_ID)))
        .password(StringUtils.trim(values.get(UserImportContract.COL_PASSWORD)))
        .remark(StringUtils.trim(values.get(UserImportContract.COL_REMARK)))
        .build();
  }
}
