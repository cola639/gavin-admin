package com.api.common.utils.excel;

import com.api.common.utils.StringUtils;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

/** Generic Excel reader using Apache POI. */
@Component
public class ExcelReader {

  public ExcelSheet read(InputStream inputStream, ExcelReadSpec spec) {
    try (Workbook workbook = WorkbookFactory.create(inputStream)) {
      Sheet sheet = resolveSheet(workbook, spec.getSheetName());
      Row headerRow = sheet.getRow(spec.getHeaderRowIndex());
      if (headerRow == null) {
        throw new IllegalArgumentException("Header row not found at index " + spec.getHeaderRowIndex());
      }

      DataFormatter formatter = new DataFormatter();
      FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

      List<String> actualHeaders = readHeaders(headerRow, formatter, evaluator);
      List<String> expectedHeaders = spec.getExpectedHeaders();
      List<String> headersToUse =
          (expectedHeaders == null || expectedHeaders.isEmpty()) ? actualHeaders : expectedHeaders;

      validateHeaders(actualHeaders, expectedHeaders, spec.isAllowExtraHeaders());

      List<ExcelRow> rows = new ArrayList<>();
      int lastRowNum = sheet.getLastRowNum();
      for (int i = spec.getHeaderRowIndex() + 1; i <= lastRowNum; i++) {
        Row row = sheet.getRow(i);
        if (row == null) {
          continue;
        }
        ExcelRow excelRow = readRow(row, headersToUse, formatter, evaluator);
        if (excelRow != null) {
          rows.add(excelRow);
        }
      }

      return new ExcelSheet(headersToUse, rows);
    } catch (ExcelHeaderMismatchException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to read Excel file: " + e.getMessage(), e);
    }
  }

  private Sheet resolveSheet(Workbook workbook, String sheetName) {
    if (StringUtils.hasText(sheetName)) {
      Sheet sheet = workbook.getSheet(sheetName);
      if (sheet == null) {
        throw new IllegalArgumentException("Sheet not found: " + sheetName);
      }
      return sheet;
    }
    if (workbook.getNumberOfSheets() == 0) {
      throw new IllegalArgumentException("Workbook has no sheets");
    }
    return workbook.getSheetAt(0);
  }

  private List<String> readHeaders(
      Row headerRow, DataFormatter formatter, FormulaEvaluator evaluator) {
    int cellCount = Math.max(headerRow.getLastCellNum(), 0);
    List<String> headers = new ArrayList<>();
    for (int i = 0; i < cellCount; i++) {
      Cell cell = headerRow.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
      String value = formatCellValue(cell, formatter, evaluator);
      headers.add(value);
    }
    return headers;
  }

  private void validateHeaders(
      List<String> actualHeaders, List<String> expectedHeaders, boolean allowExtraHeaders) {
    if (expectedHeaders == null || expectedHeaders.isEmpty()) {
      return;
    }

    if (!allowExtraHeaders && actualHeaders.size() != expectedHeaders.size()) {
      throw new ExcelHeaderMismatchException(
          "Excel header count does not match expected template", expectedHeaders, actualHeaders);
    }

    if (allowExtraHeaders && actualHeaders.size() < expectedHeaders.size()) {
      throw new ExcelHeaderMismatchException(
          "Excel header count is less than expected template", expectedHeaders, actualHeaders);
    }

    for (int i = 0; i < expectedHeaders.size(); i++) {
      String expected = normalizeHeader(expectedHeaders.get(i));
      String actual = i < actualHeaders.size() ? normalizeHeader(actualHeaders.get(i)) : "";
      if (!expected.equals(actual)) {
        throw new ExcelHeaderMismatchException(
            "Excel headers do not match expected template", expectedHeaders, actualHeaders);
      }
    }
  }

  private String normalizeHeader(String header) {
    if (!StringUtils.hasText(header)) {
      return "";
    }
    return header.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
  }

  private ExcelRow readRow(
      Row row, List<String> headers, DataFormatter formatter, FormulaEvaluator evaluator) {
    Map<String, String> values = new LinkedHashMap<>();
    boolean hasValue = false;

    for (int i = 0; i < headers.size(); i++) {
      Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
      String value = formatCellValue(cell, formatter, evaluator);
      if (StringUtils.hasText(value)) {
        hasValue = true;
      }
      values.put(headers.get(i), value);
    }

    if (!hasValue) {
      return null;
    }

    return new ExcelRow(row.getRowNum() + 1, values);
  }

  private String formatCellValue(
      Cell cell, DataFormatter formatter, FormulaEvaluator evaluator) {
    if (cell == null) {
      return "";
    }
    String value = formatter.formatCellValue(cell, evaluator);
    return value == null ? "" : value.trim();
  }
}
