package com.api.common.utils.excel;

import java.util.List;

/** Specification for reading an Excel sheet. */
public class ExcelReadSpec {

  private final String sheetName;
  private final int headerRowIndex;
  private final List<String> expectedHeaders;
  private final boolean allowExtraHeaders;

  public ExcelReadSpec(
      String sheetName, int headerRowIndex, List<String> expectedHeaders, boolean allowExtraHeaders) {
    this.sheetName = sheetName;
    this.headerRowIndex = headerRowIndex;
    this.expectedHeaders = expectedHeaders;
    this.allowExtraHeaders = allowExtraHeaders;
  }

  public String getSheetName() {
    return sheetName;
  }

  public int getHeaderRowIndex() {
    return headerRowIndex;
  }

  public List<String> getExpectedHeaders() {
    return expectedHeaders;
  }

  public boolean isAllowExtraHeaders() {
    return allowExtraHeaders;
  }
}
