package com.api.common.utils.uuid;

import java.util.UUID;

/**
 * ID Generator Utility.
 *
 * <p>Provides different styles of UUIDs: - Standard random UUID (with hyphens) - Simple UUID (no
 * hyphens) - Fast UUID (ThreadLocalRandom-based, no performance/security tradeoff in modern JDKs)
 *
 * @author
 */
public class IdUtils {

  /** Get a random UUID (with hyphens). */
  public static String randomUUID() {
    return UUID.randomUUID().toString();
  }

  /** Get a simplified UUID (without hyphens). */
  public static String simpleUUID() {
    return randomUUID().replace("-", "");
  }

  /**
   * Get a fast random UUID (in modern JDK, same as randomUUID()). Provided for semantic clarity.
   */
  public static String fastUUID() {
    return randomUUID();
  }

  /** Get a fast simplified UUID (no hyphens). */
  public static String fastSimpleUUID() {
    return simpleUUID();
  }
}
