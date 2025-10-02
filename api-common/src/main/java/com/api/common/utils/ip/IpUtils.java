package com.api.common.utils.ip;

import com.api.common.utils.ServletUtils;
import com.api.common.utils.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * IP Utility Class.
 *
 * <p>Provides: - Extract client IP from HttpServletRequest (handles proxies, load balancers) -
 * Detect internal/private IP ranges - Convert IPv4 text to byte array - Host IP & hostname
 * resolution - Support for wildcard and segment IP filtering
 *
 * @author
 */
public class IpUtils {

  private static final String REGX_0_255 = "(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)";
  public static final String REGX_IP = "((" + REGX_0_255 + "\\.){3}" + REGX_0_255 + ")";
  public static final String REGX_IP_WILDCARD =
      "(((\\*\\.){3}\\*)|("
          + REGX_0_255
          + "(\\.\\*){3})|("
          + REGX_0_255
          + "\\."
          + REGX_0_255
          + ")(\\.\\*){2}|(("
          + REGX_0_255
          + "\\.){3}\\*))";
  public static final String REGX_IP_SEG = "(" + REGX_IP + "\\-" + REGX_IP + ")";

  private static final String UNKNOWN = "unknown";

  // ======================== Client IP ======================== //

  /** Get client IP from current request. */
  public static String getIpAddr() {
    return getIpAddr(ServletUtils.getRequest());
  }

  /** Extract client IP address, considering proxy headers. */
  public static String getIpAddr(HttpServletRequest request) {
    if (request == null) {
      return UNKNOWN;
    }

    String[] headers = {
      "x-forwarded-for", "Proxy-Client-IP", "X-Forwarded-For", "WL-Proxy-Client-IP", "X-Real-IP"
    };

    String ip = null;
    for (String header : headers) {
      ip = request.getHeader(header);
      if (!isUnknown(ip)) break;
    }

    if (isUnknown(ip)) {
      ip = request.getRemoteAddr();
    }

    // Convert IPv6 localhost to IPv4
    if ("0:0:0:0:0:0:0:1".equals(ip)) {
      ip = "127.0.0.1";
    }

    return getFirstNonUnknownIp(ip);
  }

  // ======================== Internal IP ======================== //

  /** Check if IP is an internal/private address. */
  public static boolean isInternalIp(String ip) {
    byte[] addr = textToNumericFormatV4(ip);
    return isInternalIp(addr) || "127.0.0.1".equals(ip);
  }

  private static boolean isInternalIp(byte[] addr) {
    if (addr == null || addr.length < 2) return true;

    final byte b0 = addr[0], b1 = addr[1];
    // 10.x.x.x/8
    if (b0 == 0x0A) return true;
    // 172.16.x.x - 172.31.x.x
    if (b0 == (byte) 0xAC && b1 >= 0x10 && b1 <= 0x1F) return true;
    // 192.168.x.x
    return b0 == (byte) 0xC0 && b1 == (byte) 0xA8;
  }

  // ======================== IPv4 Utils ======================== //

  /** Convert IPv4 address string to byte array. */
  public static byte[] textToNumericFormatV4(String text) {
    if (StringUtils.isEmpty(text)) return null;

    String[] elements = text.split("\\.", -1);
    byte[] bytes = new byte[4];
    try {
      long l;
      switch (elements.length) {
        case 1: // a
          l = Long.parseLong(elements[0]);
          if (l < 0 || l > 0xFFFFFFFFL) return null;
          bytes[0] = (byte) (l >> 24 & 0xFF);
          bytes[1] = (byte) (l >> 16 & 0xFF);
          bytes[2] = (byte) (l >> 8 & 0xFF);
          bytes[3] = (byte) (l & 0xFF);
          break;
        case 2: // a.bbbbbb
          bytes[0] = parseByte(elements[0], 0xFF);
          l = Long.parseLong(elements[1]);
          if (l < 0 || l > 0xFFFFFFL) return null;
          bytes[1] = (byte) (l >> 16 & 0xFF);
          bytes[2] = (byte) (l >> 8 & 0xFF);
          bytes[3] = (byte) (l & 0xFF);
          break;
        case 3: // a.b.cccc
          bytes[0] = parseByte(elements[0], 0xFF);
          bytes[1] = parseByte(elements[1], 0xFF);
          l = Long.parseLong(elements[2]);
          if (l < 0 || l > 0xFFFFL) return null;
          bytes[2] = (byte) (l >> 8 & 0xFF);
          bytes[3] = (byte) (l & 0xFF);
          break;
        case 4: // a.b.c.d
          for (int i = 0; i < 4; i++) {
            bytes[i] = parseByte(elements[i], 0xFF);
          }
          break;
        default:
          return null;
      }
    } catch (NumberFormatException e) {
      return null;
    }
    return bytes;
  }

  private static byte parseByte(String value, int max) {
    long l = Long.parseLong(value);
    if (l < 0 || l > max) throw new NumberFormatException();
    return (byte) (l & 0xFF);
  }

  // ======================== Host ======================== //

  public static String getHostIp() {
    try {
      return InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      return "127.0.0.1";
    }
  }

  public static String getHostName() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      return "unknown";
    }
  }

  // ======================== Multi-Proxy ======================== //

  /** From multi-stage reverse proxy, get first non-"unknown" IP. */
  public static String getFirstNonUnknownIp(String ip) {
    if (ip != null && ip.contains(",")) {
      for (String subIp : ip.split(",")) {
        if (!isUnknown(subIp)) {
          return subIp.trim();
        }
      }
    }
    return StringUtils.substring(ip, 0, 255);
  }

  public static boolean isUnknown(String value) {
    return StringUtils.isBlank(value) || UNKNOWN.equalsIgnoreCase(value);
  }

  // ======================== Validation ======================== //

  public static boolean isIp(String ip) {
    return StringUtils.isNotBlank(ip) && ip.matches(REGX_IP);
  }

  public static boolean isIpWildcard(String ip) {
    return StringUtils.isNotBlank(ip) && ip.matches(REGX_IP_WILDCARD);
  }

  public static boolean isIpSegment(String ipSeg) {
    return StringUtils.isNotBlank(ipSeg) && ipSeg.matches(REGX_IP_SEG);
  }

  // ======================== Matching ======================== //

  public static boolean ipMatchesWildcard(String wildcard, String ip) {
    String[] w = wildcard.split("\\.");
    String[] t = ip.split("\\.");
    for (int i = 0; i < w.length && !"*".equals(w[i]); i++) {
      if (!w[i].equals(t[i])) return false;
    }
    return true;
  }

  public static boolean ipInSegment(String ipRange, String ip) {
    int idx = ipRange.indexOf('-');
    String[] start = ipRange.substring(0, idx).split("\\.");
    String[] end = ipRange.substring(idx + 1).split("\\.");
    String[] target = ip.split("\\.");

    long s = toLong(start), e = toLong(end), t = toLong(target);
    if (s > e) {
      long tmp = s;
      s = e;
      e = tmp;
    }
    return t >= s && t <= e;
  }

  private static long toLong(String[] parts) {
    long result = 0;
    for (String p : parts) {
      result = (result << 8) | Integer.parseInt(p);
    }
    return result;
  }

  /** Check if IP matches a filter rule (wildcards, ranges, exact match). */
  public static boolean matchesFilter(String filter, String ip) {
    if (StringUtils.isEmpty(filter) || StringUtils.isEmpty(ip)) return false;

    for (String rule : filter.split(";")) {
      if (isIp(rule) && rule.equals(ip)) return true;
      if (isIpWildcard(rule) && ipMatchesWildcard(rule, ip)) return true;
      if (isIpSegment(rule) && ipInSegment(rule, ip)) return true;
    }
    return false;
  }
}
