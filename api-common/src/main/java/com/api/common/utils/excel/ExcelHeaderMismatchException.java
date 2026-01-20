package com.api.common.utils.excel;

import java.util.List;

/** Thrown when Excel headers do not match the expected template. */
public class ExcelHeaderMismatchException extends RuntimeException {

  private final List<String> expected;
  private final List<String> actual;

  public ExcelHeaderMismatchException(String message, List<String> expected, List<String> actual) {
    super(message);
    this.expected = expected;
    this.actual = actual;
  }

  public List<String> getExpected() {
    return expected;
  }

  public List<String> getActual() {
    return actual;
  }
}
