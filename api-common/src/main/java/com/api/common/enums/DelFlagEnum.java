package com.api.common.enums;

/** Delete flag enum. NORMAL: record exists (not deleted) DELETED: record logically deleted */
public enum DelFlagEnum implements DictEnum<String> {
  NORMAL("Normal", "Normal"),
  DELETED("Deleted", "Deleted");

  private final String code;
  private final String description;

  DelFlagEnum(String code, String description) {
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
