package com.api.framework.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Wraps JSON requests to allow multiple reads of request body (for interceptors and controllers).
 */
@Component
@Order(1)
public class RequestWrapperFilter implements Filter {
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if (request instanceof HttpServletRequest req
        && req.getContentType() != null
        && req.getContentType().contains("application/json")) {
      chain.doFilter(new RepeatableRequestWrapper(req), response);
    } else {
      chain.doFilter(request, response);
    }
  }
}
