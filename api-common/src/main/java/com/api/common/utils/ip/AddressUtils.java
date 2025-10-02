package com.api.common.utils.ip;

import com.api.common.constant.Constants;
import com.api.common.utils.StringUtils;
import com.api.common.config.AppConfig;
import com.api.common.utils.http.HttpUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for resolving geographic location from an IP address.
 *
 * <p>- Uses external IP lookup API (pconline) - Returns "Internal IP" for private/internal
 * addresses - Returns "XX XX" if lookup fails or is disabled
 */
public class AddressUtils {

  private static final Logger log = LoggerFactory.getLogger(AddressUtils.class);

  /** External IP lookup service */
  private static final String IP_LOOKUP_URL = "http://whois.pconline.com.cn/ipJson.jsp";

  /** Default value for unknown location */
  private static final String UNKNOWN = "XX XX";

  /** Reusable Jackson ObjectMapper */
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  /**
   * Get geographic location by IP address.
   *
   * @param ip IP address
   * @return location string (e.g. "Beijing Beijing") or "Internal IP" / "XX XX"
   */
  public static String getRealAddressByIP(String ip) {
    // Skip internal/private IP addresses
    if (IpUtils.isInternalIp(ip)) {
      return "Internal IP";
    }

    // If external address lookup is enabled
    if (AppConfig.getAddressEnabled()) {
      try {
        String response =
            HttpUtils.sendGet(IP_LOOKUP_URL, "ip=" + ip + "&json=true", Constants.GBK);

        if (StringUtils.isEmpty(response)) {
          log.error("Failed to retrieve geographic location for IP: {}", ip);
          return UNKNOWN;
        }

        // Parse JSON using Jackson
        JsonNode root = OBJECT_MAPPER.readTree(response);
        String region = root.path("pro").asText("");
        String city = root.path("city").asText("");

        return String.format("%s %s", region, city).trim();

      } catch (Exception e) {
        log.error("Error while retrieving geographic location for IP: {}", ip, e);
      }
    }

    return UNKNOWN;
  }
}
