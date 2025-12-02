package com.api.common.enums;

public enum UserStatusEnum implements DictEnum<String> {
  NORMAL("Enabled", "Enabled"),
  DISABLED("Disabled", "Disabled");

  private final String code;
  private final String description;

  UserStatusEnum(String code, String description) {
    this.code = code;
    this.description = description;
  }

  /** Code stored in database. */
  @Override
  public String getCode() {
    return code;
  }

  /** Description for human reading. */
  public String getDescription() {
    return description;
  }
}
