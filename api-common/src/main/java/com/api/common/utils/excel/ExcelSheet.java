package com.api.common.utils.excel;

import java.util.List;

/** Excel sheet data including headers and rows. */
public class ExcelSheet {

  private final List<String> headers;
  private final List<ExcelRow> rows;

  public ExcelSheet(List<String> headers, List<ExcelRow> rows) {
    this.headers = headers;
    this.rows = rows;
  }

  public List<String> getHeaders() {
    return headers;
  }

  public List<ExcelRow> getRows() {
    return rows;
  }
}
