package com.api.common.enums.upload;

import java.util.Arrays;

public enum FileCategoryEnum {
  AVATAR,
  EXCEL,
  DOC,
  FILE,
  ATTACHMENT;

  public static FileCategoryEnum parse(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("category is required");
    }
    return Arrays.stream(values())
        .filter(v -> v.name().equalsIgnoreCase(value))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unsupported category: " + value));
  }
}
