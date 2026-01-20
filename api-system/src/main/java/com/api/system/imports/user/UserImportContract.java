package com.api.system.imports.user;

import com.api.common.enums.UserStatusEnum;
import com.api.common.enums.UserTypeEnum;
import com.api.common.utils.StringUtils;
import com.api.common.utils.excel.ExcelReadSpec;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Data contract for user Excel imports. */
public final class UserImportContract {

  public static final String SHEET_NAME = "UserImport";

  public static final String COL_USER_NAME = "User Name";
  public static final String COL_NICK_NAME = "Nick Name";
  public static final String COL_EMAIL = "Email";
  public static final String COL_PHONE_NUMBER = "Phone Number";
  public static final String COL_STATUS = "Status";
  public static final String COL_SEX = "Sex";
  public static final String COL_USER_TYPE = "User Type";
  public static final String COL_DEPT_ID = "Dept Id";
  public static final String COL_PASSWORD = "Password";
  public static final String COL_REMARK = "Remark";

  public static final List<String> HEADERS =
      List.of(
          COL_USER_NAME,
          COL_NICK_NAME,
          COL_EMAIL,
          COL_PHONE_NUMBER,
          COL_STATUS,
          COL_SEX,
          COL_USER_TYPE,
          COL_DEPT_ID,
          COL_PASSWORD,
          COL_REMARK);

  public static final Set<String> REQUIRED_HEADERS = Set.of(COL_USER_NAME, COL_NICK_NAME);

  public static final List<String> STATUS_ALLOWED =
      List.of(UserStatusEnum.NORMAL.getCode(), UserStatusEnum.DISABLED.getCode(), "0", "1");
  public static final List<String> SEX_ALLOWED =
      List.of("0", "1", "2", "Male", "Female", "Unknown");
  public static final List<String> USER_TYPE_ALLOWED =
      List.of(UserTypeEnum.SYSTEM.getCode(), UserTypeEnum.GITHUB.getCode());

  public static final String DEFAULT_STATUS = UserStatusEnum.NORMAL.getCode();
  public static final String DEFAULT_USER_TYPE = UserTypeEnum.SYSTEM.getCode();
  public static final String DEFAULT_SEX = "2";

  public static final List<String> SYSTEM_OWNED_FIELDS =
      List.of(
          "user_id",
          "del_flag",
          "create_by",
          "create_time",
          "update_by",
          "update_time",
          "login_ip",
          "login_date",
          "pwd_update_date",
          "avatar",
          "oauth_id");

  private static final Map<String, String> STATUS_ALIASES =
      Map.of(
          "enabled", UserStatusEnum.NORMAL.getCode(),
          "disabled", UserStatusEnum.DISABLED.getCode(),
          "0", UserStatusEnum.NORMAL.getCode(),
          "1", UserStatusEnum.DISABLED.getCode());

  private static final Map<String, String> SEX_ALIASES =
      Map.ofEntries(
          Map.entry("0", "0"),
          Map.entry("1", "1"),
          Map.entry("2", "2"),
          Map.entry("male", "0"),
          Map.entry("m", "0"),
          Map.entry("female", "1"),
          Map.entry("f", "1"),
          Map.entry("unknown", "2"),
          Map.entry("u", "2"));

  private static final Map<String, String> USER_TYPE_ALIASES =
      Map.of(
          "system", UserTypeEnum.SYSTEM.getCode(),
          "github", UserTypeEnum.GITHUB.getCode(),
          "local", UserTypeEnum.SYSTEM.getCode());

  private UserImportContract() {}

  public static ExcelReadSpec readSpec() {
    return new ExcelReadSpec(SHEET_NAME, 0, HEADERS, false);
  }

  public static String normalizeStatus(String value) {
    return normalizeWithAliases(value, STATUS_ALIASES);
  }

  public static String normalizeSex(String value) {
    return normalizeWithAliases(value, SEX_ALIASES);
  }

  public static String normalizeUserType(String value) {
    return normalizeWithAliases(value, USER_TYPE_ALIASES);
  }

  private static String normalizeWithAliases(String value, Map<String, String> aliases) {
    if (!StringUtils.hasText(value)) {
      return null;
    }
    String key = value.trim().toLowerCase(Locale.ROOT);
    return aliases.get(key);
  }
}
