package com.api.common.enums;

/** Common interface for dictionary enums. */
public interface DictEnum<T> {

  /** Get code that is stored in database. */
  T getCode();
}
