package com.api.framework.enums;

/** Strategy for identifying the rate-limited requester. */
public enum LimitType {
  /** Default: use method signature as the key. */
  DEFAULT,

  /** Limit by client IP address. */
  IP,

  /** Limit by authenticated user ID. */
  USER
}
