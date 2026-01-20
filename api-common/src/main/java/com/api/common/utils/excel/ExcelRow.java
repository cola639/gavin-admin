package com.api.common.utils.excel;

import java.util.Map;

/** Single Excel row with row index (1-based) and header-mapped values. */
public class ExcelRow {

  private final int rowIndex;
  private final Map<String, String> values;

  public ExcelRow(int rowIndex, Map<String, String> values) {
    this.rowIndex = rowIndex;
    this.values = values;
  }

  public int getRowIndex() {
    return rowIndex;
  }

  public Map<String, String> getValues() {
    return values;
  }
}
