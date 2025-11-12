package com.api.framework.config;

import com.api.common.utils.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 🌐 Server configuration utility.
 *
 * <p>Provides methods to resolve the full base URL of incoming requests, including protocol,
 * domain, port, and context path.
 *
 * <p>Example:
 *
 * <pre>
 *   Input:  http://localhost:8080/api/test
 *   Output: http://localhost:8080/api
 * </pre>
 */
@Slf4j
@Component
public class ServerConfig {

  /**
   * Returns the complete base URL of the current request.
   *
   * <p>This method automatically retrieves the current {@link HttpServletRequest} from {@link
   * ServletUtils} and constructs a URL that includes:
   *
   * <ul>
   *   <li>Protocol (HTTP/HTTPS)
   *   <li>Domain or IP
   *   <li>Port number (if not default)
   *   <li>Context path
   * </ul>
   *
   * @return The full request base URL (e.g. {@code http://localhost:8080/app})
   */
  public String getUrl() {
    HttpServletRequest request = ServletUtils.getRequest();
    if (request == null) {
      log.warn("⚠️ Unable to determine request URL — no active HTTP request found.");
      return "";
    }
    return buildBaseUrl(request);
  }

  /**
   * Constructs the base URL from the given request.
   *
   * @param request the HTTP request
   * @return the base URL (e.g. {@code http://example.com:8080/app})
   */
  public static String buildBaseUrl(HttpServletRequest request) {
    if (request == null) {
      return "";
    }

    StringBuffer requestUrl = request.getRequestURL();
    String requestUri = request.getRequestURI();
    String contextPath = request.getContextPath();

    // Remove URI from the end of the URL (keeps protocol, host, and port)
    String baseUrl =
        requestUrl
            .delete(requestUrl.length() - requestUri.length(), requestUrl.length())
            .toString();

    // Append context path if it exists
    if (contextPath != null && !contextPath.isBlank()) {
      baseUrl += contextPath;
    }

    log.debug("🔗 Resolved server base URL: {}", baseUrl);
    return baseUrl;
  }
}
