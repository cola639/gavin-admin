package com.api.common.utils.http;

import com.api.common.constant.Constants;
import com.api.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * Simplified HTTP utility using Spring's RestTemplate.
 *
 * <p>Provides convenient GET and POST methods: - Handles UTF-8 encoding by default - Configurable
 * content type - Logs request/response
 *
 * <p>For SSL requests, use https:// URLs (Java truststore handles certificates).
 *
 * @author
 */
@Component
public class HttpUtils {

  private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);

  private static final RestTemplate restTemplate = new RestTemplate();

  /** Send a simple GET request without params. */
  public static String sendGet(String url) {
    return sendGet(url, null, Constants.UTF8);
  }

  /**
   * Send GET request with query params string.
   *
   * @param url base URL
   * @param param query params (e.g. "name=Tom&age=18")
   * @param charset response charset (default UTF-8)
   */
  public static String sendGet(String url, String param, String charset) {
    String requestUrl = StringUtils.isNotBlank(param) ? url + "?" + param : url;
    log.info("sendGet -> {}", requestUrl);

    ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);
    String body = response.getBody();

    log.info("recv <- {}", body);
    return body != null
        ? new String(body.getBytes(StandardCharsets.UTF_8), Charset.forName(charset))
        : "";
  }

  /**
   * Send POST request with form data.
   *
   * @param url target URL
   * @param param form body string (e.g. "name=Tom&age=18")
   */
  public static String sendPost(String url, String param) {
    return sendPost(url, param, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
  }

  /**
   * Send POST request with custom content type.
   *
   * @param url target URL
   * @param param body string
   * @param contentType content type (e.g. application/json)
   */
  public static String sendPost(String url, String param, String contentType) {
    log.info("sendPost -> {}", url);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType(contentType));
    headers.setAccept(Collections.singletonList(MediaType.ALL));
    headers.set(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Spring Boot Client)");

    HttpEntity<String> entity = new HttpEntity<>(param, headers);

    ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
    String body = response.getBody();

    log.info("recv <- {}", body);
    return body != null ? body : "";
  }

  /** Send HTTPS POST request (SSL handled by JVM truststore). */
  public static String sendSSLPost(String url, String param) {
    // No need for custom TrustManager if certificates are valid
    return sendPost(url, param, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
  }
}
