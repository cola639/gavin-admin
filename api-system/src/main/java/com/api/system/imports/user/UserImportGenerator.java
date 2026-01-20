package com.api.system.imports.user;

import com.api.common.domain.SysUser;
import com.api.common.enums.DelFlagEnum;
import com.api.common.utils.SecurityUtils;
import com.api.common.utils.StringUtils;
import com.api.system.imports.base.ImportRowGenerator;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Normalize values and apply defaults before persistence. */
@Component
public class UserImportGenerator
    implements ImportRowGenerator<UserImportRow, UserImportContext, SysUser> {

  @Value("${app.default.password:admin123}")
  private String defaultPassword;

  @Override
  public SysUser generate(UserImportRow row, UserImportContext context, String operator) {
    SysUser existingUser = context == null ? null : context.getExistingUser();
    Long deptId = context == null ? null : context.getDeptId();
    boolean isUpdate = existingUser != null;
    SysUser target = isUpdate ? existingUser : new SysUser();

    String userName = StringUtils.trim(row.getUserName());
    String nickName = StringUtils.trim(row.getNickName());

    target.setUserName(userName);
    target.setNickName(nickName);

    String email = normalizeEmail(row.getEmail());
    if (StringUtils.hasText(email)) {
      target.setEmail(email);
    } else if (!isUpdate) {
      target.setEmail(null);
    }

    String phone = StringUtils.trim(row.getPhoneNumber());
    if (StringUtils.hasText(phone)) {
      target.setPhonenumber(phone);
    } else if (!isUpdate) {
      target.setPhonenumber(null);
    }

    if (StringUtils.hasText(row.getStatus())) {
      target.setStatus(UserImportContract.normalizeStatus(row.getStatus()));
    } else if (!isUpdate) {
      target.setStatus(UserImportContract.DEFAULT_STATUS);
    }

    if (StringUtils.hasText(row.getSex())) {
      target.setSex(UserImportContract.normalizeSex(row.getSex()));
    } else if (!isUpdate) {
      target.setSex(UserImportContract.DEFAULT_SEX);
    }

    if (StringUtils.hasText(row.getUserType())) {
      target.setUserType(UserImportContract.normalizeUserType(row.getUserType()));
    } else if (!isUpdate) {
      target.setUserType(UserImportContract.DEFAULT_USER_TYPE);
    }

    if (deptId != null) {
      target.setDeptId(deptId);
    }

    if (StringUtils.hasText(row.getPassword())) {
      target.setPassword(SecurityUtils.encryptPassword(row.getPassword().trim()));
    } else if (!isUpdate) {
      target.setPassword(SecurityUtils.encryptPassword(defaultPassword));
    }

    if (StringUtils.hasText(row.getRemark())) {
      target.setRemark(row.getRemark().trim());
    } else if (!isUpdate) {
      target.setRemark(null);
    }

    if (!isUpdate) {
      target.setDelFlag(DelFlagEnum.NORMAL.getCode());
      target.setCreateBy(operator);
    } else {
      target.setUpdateBy(operator);
    }

    return target;
  }

  private String normalizeEmail(String email) {
    if (!StringUtils.hasText(email)) {
      return "";
    }
    return email.trim().toLowerCase(Locale.ROOT);
  }
}
