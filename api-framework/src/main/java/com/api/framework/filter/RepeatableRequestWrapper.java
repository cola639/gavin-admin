package com.api.framework.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.*;

/** Caches request body so it can be read multiple times. */
public class RepeatableRequestWrapper extends HttpServletRequestWrapper {

  private final byte[] cachedBody;

  public RepeatableRequestWrapper(HttpServletRequest request) throws IOException {
    super(request);
    this.cachedBody = request.getInputStream().readAllBytes();
  }

  @Override
  public ServletInputStream getInputStream() {
    ByteArrayInputStream bais = new ByteArrayInputStream(cachedBody);
    return new ServletInputStream() {
      @Override
      public int read() {
        return bais.read();
      }

      @Override
      public boolean isFinished() {
        return bais.available() == 0;
      }

      @Override
      public boolean isReady() {
        return true;
      }

      @Override
      public void setReadListener(ReadListener listener) {}
    };
  }

  @Override
  public BufferedReader getReader() {
    return new BufferedReader(new InputStreamReader(getInputStream()));
  }

  public String getBodyString() {
    return new String(cachedBody);
  }
}
