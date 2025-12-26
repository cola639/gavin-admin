package com.api.common.enums;

public enum UserTypeEnum implements DictEnum<String> {
  SYSTEM("System", "System / local user"),
  GITHUB("Github", "GitHub OAuth2 user");

  private final String code;
  private final String description;

  UserTypeEnum(String code, String description) {
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
