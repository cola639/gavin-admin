package com.api.system.imports.user;

import com.api.common.constant.UserConstants;
import com.api.common.domain.SysUser;
import com.api.common.enums.DelFlagEnum;
import com.api.common.utils.StringUtils;
import com.api.system.imports.base.ImportBatchContext;
import com.api.system.imports.base.ImportRowValidator;
import com.api.system.imports.base.ImportValidationResult;
import com.api.system.repository.SysDeptRepository;
import com.api.system.repository.SysUserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Validate user import rows (required fields, allowed values, and uniqueness). */
@Component
@RequiredArgsConstructor
public class UserImportValidator implements ImportRowValidator<UserImportRow, UserImportContext> {

  private static final int USER_NAME_MAX = 30;
  private static final int NICK_NAME_MAX = 30;
  private static final int EMAIL_MAX = 50;
  private static final int PHONE_MAX = 11;

  private final SysUserRepository userRepository;
  private final SysDeptRepository deptRepository;

  @Override
  public ImportValidationResult<UserImportContext> validate(
      UserImportRow row, ImportBatchContext batchContext) {
    List<String> errors = new ArrayList<>();
    boolean updateSupport = batchContext.isUpdateSupport();
    Set<String> seenUserNames = batchContext.getSeenKeys();

    String userName = StringUtils.trim(row.getUserName());
    if (!StringUtils.hasText(userName)) {
      errors.add("user_name is required");
    } else {
      if (userName.length() > USER_NAME_MAX) {
        errors.add("user_name length must be <= " + USER_NAME_MAX);
      }
      String userKey = userName.toLowerCase(Locale.ROOT);
      if (seenUserNames.contains(userKey)) {
        errors.add("duplicate user_name in file");
      } else {
        seenUserNames.add(userKey);
      }
    }

    String nickName = StringUtils.trim(row.getNickName());
    if (!StringUtils.hasText(nickName)) {
      errors.add("nick_name is required");
    } else if (nickName.length() > NICK_NAME_MAX) {
      errors.add("nick_name length must be <= " + NICK_NAME_MAX);
    }

    String email = normalizeEmail(row.getEmail());
    if (StringUtils.hasText(email) && email.length() > EMAIL_MAX) {
      errors.add("email length must be <= " + EMAIL_MAX);
    }

    String phone = StringUtils.trim(row.getPhoneNumber());
    if (StringUtils.hasText(phone) && phone.length() > PHONE_MAX) {
      errors.add("phone_number length must be <= " + PHONE_MAX);
    }

    if (StringUtils.hasText(row.getStatus())
        && UserImportContract.normalizeStatus(row.getStatus()) == null) {
      errors.add("status must be one of: " + String.join(", ", UserImportContract.STATUS_ALLOWED));
    }

    if (StringUtils.hasText(row.getSex())
        && UserImportContract.normalizeSex(row.getSex()) == null) {
      errors.add("sex must be one of: " + String.join(", ", UserImportContract.SEX_ALLOWED));
    }

    if (StringUtils.hasText(row.getUserType())
        && UserImportContract.normalizeUserType(row.getUserType()) == null) {
      errors.add(
          "user_type must be one of: " + String.join(", ", UserImportContract.USER_TYPE_ALLOWED));
    }

    if (StringUtils.hasText(row.getPassword())) {
      int len = row.getPassword().trim().length();
      if (len < UserConstants.PASSWORD_MIN_LENGTH || len > UserConstants.PASSWORD_MAX_LENGTH) {
        errors.add(
            "password length must be between "
                + UserConstants.PASSWORD_MIN_LENGTH
                + " and "
                + UserConstants.PASSWORD_MAX_LENGTH);
      }
    }

    Long deptId = null;
    if (StringUtils.hasText(row.getDeptId())) {
      try {
        deptId = Long.parseLong(row.getDeptId().trim());
      } catch (NumberFormatException e) {
        errors.add("dept_id must be a number");
      }
      if (deptId != null && !deptRepository.existsById(deptId)) {
        errors.add("dept_id not found: " + deptId);
      }
    }

    SysUser existingUser = null;
    if (StringUtils.hasText(userName)) {
      existingUser =
          userRepository
              .findByUserNameAndDelFlag(userName, DelFlagEnum.NORMAL.getCode())
              .orElse(null);

      boolean userNameExists = userRepository.existsByUserName(userName);
      if (!updateSupport && userNameExists) {
        errors.add("user_name already exists");
      }
      if (updateSupport && existingUser == null && userNameExists) {
        errors.add("user_name exists but is deleted or unavailable");
      }
    }

    if (StringUtils.hasText(email)) {
      Optional<SysUser> emailOwner = userRepository.findByEmail(email);
      if (emailOwner.isPresent()
          && (existingUser == null
              || !emailOwner.get().getUserId().equals(existingUser.getUserId()))) {
        errors.add("email already exists");
      }
    }

    if (StringUtils.hasText(phone)) {
      Optional<SysUser> phoneOwner =
          userRepository.findByPhonenumberAndDelFlag(phone, DelFlagEnum.NORMAL.getCode());
      if (phoneOwner.isPresent()
          && (existingUser == null
              || !phoneOwner.get().getUserId().equals(existingUser.getUserId()))) {
        errors.add("phone number already exists");
      }
    }

    return new ImportValidationResult<>(errors, new UserImportContext(existingUser, deptId));
  }

  private String normalizeEmail(String email) {
    if (!StringUtils.hasText(email)) {
      return "";
    }
    return email.trim().toLowerCase(Locale.ROOT);
  }
}
