package com.api.common.utils.uuid;

import com.api.common.exceptions.UtilException;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Custom UUID implementation (Universally Unique Identifier).
 *
 * <p>Supports: - Version 3 (name-based, MD5) - Version 4 (random) - Secure or fast random
 * generators
 *
 * <p>NOTE: In most cases, prefer {@link java.util.UUID} or Hutool's IdUtil for simplicity.
 *
 * @author
 */
public final class UUID implements Serializable, Comparable<UUID> {

  private static final long serialVersionUID = -1185015143654744140L;

  /** High 64 bits of this UUID */
  private final long mostSigBits;

  /** Low 64 bits of this UUID */
  private final long leastSigBits;

  /** Holder for SecureRandom singleton */
  private static class Holder {
    static final SecureRandom numberGenerator = getSecureRandom();
  }

  /** Private constructor from raw bytes */
  private UUID(byte[] data) {
    long msb = 0;
    long lsb = 0;
    assert data.length == 16 : "data must be 16 bytes";
    for (int i = 0; i < 8; i++) {
      msb = (msb << 8) | (data[i] & 0xff);
    }
    for (int i = 8; i < 16; i++) {
      lsb = (lsb << 8) | (data[i] & 0xff);
    }
    this.mostSigBits = msb;
    this.leastSigBits = lsb;
  }

  /** Public constructor from explicit bits */
  public UUID(long mostSigBits, long leastSigBits) {
    this.mostSigBits = mostSigBits;
    this.leastSigBits = leastSigBits;
  }

  // ------------------- Factory Methods -------------------

  /** Generate a fast random UUID (version 4, ThreadLocalRandom). */
  public static UUID fastUUID() {
    return randomUUID(false);
  }

  /** Generate a secure random UUID (version 4, SecureRandom). */
  public static UUID randomUUID() {
    return randomUUID(true);
  }

  /**
   * Generate a random UUID (version 4).
   *
   * @param isSecure true for SecureRandom, false for ThreadLocalRandom
   */
  public static UUID randomUUID(boolean isSecure) {
    final Random ng = isSecure ? Holder.numberGenerator : getRandom();
    byte[] randomBytes = new byte[16];
    ng.nextBytes(randomBytes);

    // Set version and variant bits
    randomBytes[6] &= 0x0f;
    randomBytes[6] |= 0x40;
    randomBytes[8] &= 0x3f;
    randomBytes[8] |= 0x80;

    return new UUID(randomBytes);
  }

  /** Generate a name-based UUID (version 3, MD5 hash of input). */
  public static UUID nameUUIDFromBytes(byte[] name) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] md5Bytes = md.digest(name);
      md5Bytes[6] &= 0x0f;
      md5Bytes[6] |= 0x30;
      md5Bytes[8] &= 0x3f;
      md5Bytes[8] |= 0x80;
      return new UUID(md5Bytes);
    } catch (NoSuchAlgorithmException e) {
      throw new InternalError("MD5 not supported", e);
    }
  }

  /** Parse a UUID from its canonical string representation. */
  public static UUID fromString(String name) {
    String[] components = name.split("-");
    if (components.length != 5) {
      throw new IllegalArgumentException("Invalid UUID string: " + name);
    }

    long mostSigBits = Long.decode("0x" + components[0]);
    mostSigBits = (mostSigBits << 16) | Long.decode("0x" + components[1]);
    mostSigBits = (mostSigBits << 16) | Long.decode("0x" + components[2]);

    long leastSigBits = Long.decode("0x" + components[3]);
    leastSigBits = (leastSigBits << 48) | Long.decode("0x" + components[4]);

    return new UUID(mostSigBits, leastSigBits);
  }

  // ------------------- Accessors -------------------

  public long getLeastSignificantBits() {
    return leastSigBits;
  }

  public long getMostSignificantBits() {
    return mostSigBits;
  }

  /** Return UUID version (1: time-based, 3: name-based, 4: random). */
  public int version() {
    return (int) ((mostSigBits >> 12) & 0x0f);
  }

  /** Return UUID variant (RFC4122, Microsoft, etc.). */
  public int variant() {
    return (int) ((leastSigBits >>> (64 - (leastSigBits >>> 62))) & (leastSigBits >> 63));
  }

  /** Time-based UUID timestamp (only valid for version 1). */
  public long timestamp() {
    checkTimeBase();
    return (mostSigBits & 0x0FFFL) << 48
        | ((mostSigBits >> 16) & 0x0FFFFL) << 32
        | mostSigBits >>> 32;
  }

  /** Time-based UUID clock sequence (only valid for version 1). */
  public int clockSequence() {
    checkTimeBase();
    return (int) ((leastSigBits & 0x3FFF000000000000L) >>> 48);
  }

  /** Time-based UUID node identifier (only valid for version 1). */
  public long node() {
    checkTimeBase();
    return leastSigBits & 0x0000FFFFFFFFFFFFL;
  }

  // ------------------- String & Comparison -------------------

  @Override
  public String toString() {
    return toString(false);
  }

  /** Convert UUID to string, optionally simple (no hyphens). */
  public String toString(boolean simple) {
    StringBuilder builder = new StringBuilder(simple ? 32 : 36);
    builder.append(digits(mostSigBits >> 32, 8));
    if (!simple) builder.append('-');
    builder.append(digits(mostSigBits >> 16, 4));
    if (!simple) builder.append('-');
    builder.append(digits(mostSigBits, 4));
    if (!simple) builder.append('-');
    builder.append(digits(leastSigBits >> 48, 4));
    if (!simple) builder.append('-');
    builder.append(digits(leastSigBits, 12));
    return builder.toString();
  }

  @Override
  public int hashCode() {
    long hilo = mostSigBits ^ leastSigBits;
    return ((int) (hilo >> 32)) ^ (int) hilo;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof UUID uuid)) return false;
    return mostSigBits == uuid.mostSigBits && leastSigBits == uuid.leastSigBits;
  }

  @Override
  public int compareTo(UUID val) {
    return Long.compare(this.mostSigBits, val.mostSigBits) != 0
        ? Long.compare(this.mostSigBits, val.mostSigBits)
        : Long.compare(this.leastSigBits, val.leastSigBits);
  }

  // ------------------- Helpers -------------------

  private static String digits(long val, int digits) {
    long hi = 1L << (digits * 4);
    return Long.toHexString(hi | (val & (hi - 1))).substring(1);
  }

  private void checkTimeBase() {
    if (version() != 1) {
      throw new UnsupportedOperationException("Not a time-based UUID");
    }
  }

  public static SecureRandom getSecureRandom() {
    try {
      return SecureRandom.getInstance("SHA1PRNG");
    } catch (NoSuchAlgorithmException e) {
      throw new UtilException(e);
    }
  }

  public static ThreadLocalRandom getRandom() {
    return ThreadLocalRandom.current();
  }
}
