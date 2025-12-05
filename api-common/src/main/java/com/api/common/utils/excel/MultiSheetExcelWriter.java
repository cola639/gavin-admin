package com.api.common.utils.excel;

import jakarta.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * MultiSheetExcelWriter A utility for exporting multiple sheets into one Excel file. Support
 * different entity types for each sheet.
 *
 * @author demo
 * @date 2025/08/28
 */
@Slf4j
public class MultiSheetExcelWriter {

  /**
   * Export multiple sheets into HttpServletResponse.
   *
   * @param sheetDataMap map of sheetName -> List data
   * @param clazzMap map of sheetName -> Class type
   * @param resp HttpServletResponse
   * @param fileName Excel file name
   * @param dictProvider Dictionary provider for label/color conversion
   */
  public static void export(
      Map<String, List<?>> sheetDataMap,
      Map<String, Class<?>> clazzMap,
      HttpServletResponse resp,
      String fileName,
      DictProvider dictProvider) {
    resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    resp.setCharacterEncoding("utf-8");
    try (SXSSFWorkbook wb = new SXSSFWorkbook(200)) {
      // iterate all sheets
      for (Map.Entry<String, List<?>> entry : sheetDataMap.entrySet()) {
        String sheetName = entry.getKey();
        List<?> data = entry.getValue();
        Class<?> clazz = clazzMap.get(sheetName);

        if (clazz == null) {
          log.warn("No class mapping found for sheetName: {}", sheetName);
          continue;
        }

        log.info("Writing sheet: {}, data size: {}", sheetName, data != null ? data.size() : 0);

        // delegate to generic bridge method
        writeSheet(wb, data, clazz, sheetName, dictProvider);
      }

      // encode filename
      String encoded =
          URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replace("+", "%20");
      resp.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + encoded + ".xlsx");

      wb.write(resp.getOutputStream());
    } catch (Exception e) {
      log.error("MultiSheet export failed", e);
      throw new RuntimeException("Export failed", e);
    }
  }

  /**
   * Export multiple sheets into any OutputStream.
   *
   * @param sheetDataMap map of sheetName -> List data
   * @param clazzMap map of sheetName -> Class type
   * @param os output stream
   * @param dictProvider Dictionary provider
   */
  public static void export(
      Map<String, List<?>> sheetDataMap,
      Map<String, Class<?>> clazzMap,
      OutputStream os,
      DictProvider dictProvider) {
    try (SXSSFWorkbook wb = new SXSSFWorkbook(200)) {
      for (Map.Entry<String, List<?>> entry : sheetDataMap.entrySet()) {
        String sheetName = entry.getKey();
        List<?> data = entry.getValue();
        Class<?> clazz = clazzMap.get(sheetName);

        if (clazz == null) {
          log.warn("No class mapping found for sheetName: {}", sheetName);
          continue;
        }

        log.info("Writing sheet: {}, data size: {}", sheetName, data != null ? data.size() : 0);

        writeSheet(wb, data, clazz, sheetName, dictProvider);
      }

      wb.write(os);
    } catch (Exception e) {
      log.error("MultiSheet export failed", e);
      throw new RuntimeException("Export failed", e);
    }
  }

  /** Generic bridge to call SimpleExcelWriter.writeWorkbook with correct type. */
  @SuppressWarnings("unchecked")
  private static <T> void writeSheet(
      SXSSFWorkbook wb,
      List<?> rawData,
      Class<?> rawClazz,
      String sheetName,
      DictProvider dictProvider)
      throws Exception {
    SimpleExcelWriter.writeWorkbook(
        wb, (List<T>) rawData, (Class<T>) rawClazz, sheetName, dictProvider);
  }
}
