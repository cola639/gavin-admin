package com.api.common.domain.email;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Locale;

public enum EmailBodyMode {
  HTML_ONLY,
  TEXT_ONLY,
  BOTH;

  @JsonCreator
  public static EmailBodyMode from(String raw) {
    if (raw == null || raw.isBlank()) {
      return BOTH;
    }
    var v = raw.trim().toUpperCase(Locale.ROOT);
    return switch (v) {
      case "HTML", "HTML_ONLY" -> HTML_ONLY;
      case "TEXT", "TEXT_ONLY" -> TEXT_ONLY;
      case "BOTH" -> BOTH;
      default -> throw new IllegalArgumentException("Unsupported EmailBodyMode: " + raw);
    };
  }
}
