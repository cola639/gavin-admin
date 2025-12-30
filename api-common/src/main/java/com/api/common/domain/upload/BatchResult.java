package com.api.common.domain.upload;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BatchResult<T> {
  boolean success;
  String message;
  T data;

  public static <T> BatchResult<T> ok(T data) {
    return BatchResult.<T>builder().success(true).message("OK").data(data).build();
  }

  public static <T> BatchResult<T> fail(String message) {
    return BatchResult.<T>builder().success(false).message(message).build();
  }
}
