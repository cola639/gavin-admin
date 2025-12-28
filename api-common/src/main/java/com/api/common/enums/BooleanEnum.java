package com.api.common.enums;

public enum BooleanEnum {
  TRUE("True"),
  FALSE("False");

  private String value;

  BooleanEnum(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
