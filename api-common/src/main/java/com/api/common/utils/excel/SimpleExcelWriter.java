package com.api.common.utils.excel;

import com.api.common.annotation.Xls;
import com.api.common.annotation.XlsRichText;
import jakarta.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

public class SimpleExcelWriter {

  /** 导出入口（写到响应） */
  public static <T> void export(
      List<T> data,
      Class<T> clazz,
      HttpServletResponse resp,
      String fileName,
      String sheetName,
      DictProvider dictProvider) {
    resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    resp.setCharacterEncoding("utf-8");
    try (SXSSFWorkbook wb = new SXSSFWorkbook(200)) {
      writeWorkbook(wb, data, clazz, sheetName, dictProvider);

      String encoded =
          URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replace("+", "%20");
      resp.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + encoded + ".xlsx");
      wb.write(resp.getOutputStream());
    } catch (Exception e) {
      throw new RuntimeException("导出失败", e);
    }
  }

  /** 导出入口（写到任意输出流） */
  public static <T> void export(
      List<T> data, Class<T> clazz, OutputStream os, String sheetName, DictProvider dictProvider) {
    try (SXSSFWorkbook wb = new SXSSFWorkbook(200)) {
      writeWorkbook(wb, data, clazz, sheetName, dictProvider);
      wb.write(os);
    } catch (Exception e) {
      throw new RuntimeException("导出失败", e);
    }
  }

  /* ================== 内部实现 ================== */

  public static <T> void writeWorkbook(
      Workbook wb, List<T> data, Class<T> clazz, String sheetName, DictProvider dictProvider)
      throws Exception {
    if (data == null) data = Collections.emptyList();
    Sheet sheet = wb.createSheet(sheetName == null ? "Sheet1" : sheetName);

    // 1) 解析字段元数据
    List<Col> cols = parseCols(clazz, data);

    // 2) 样式
    Styles styles = buildStyles(wb, cols);

    // 3) 表头
    Row header = sheet.createRow(0);

    // === 设置表头高度（取最大值）===
    int maxHeaderHeight = cols.stream().mapToInt(c -> c.height).filter(h -> h > 0).max().orElse(-1);

    if (maxHeaderHeight > 0) {
      header.setHeightInPoints(maxHeaderHeight);
    }

    for (int i = 0; i < cols.size(); i++) {
      Cell cell = header.createCell(i);
      Col col = cols.get(i);

      if (col.headerRichTexts != null && col.headerRichTexts.length > 0) {
        // ========== 构建 RichText ==========
        XSSFRichTextString rich = new XSSFRichTextString();
        for (XlsRichText rt : col.headerRichTexts) {
          XSSFFont xf = (XSSFFont) wb.createFont();
          xf.setFontName(col.headerFontFamily != null ? col.headerFontFamily : "Arial");
          xf.setFontHeightInPoints(rt.fontSize());
          xf.setBold(rt.bold());
          xf.setItalic(rt.italic());

          // 设置颜色
          if (rt.color().startsWith("#") && rt.color().length() == 7) {
            java.awt.Color awtColor =
                new java.awt.Color(
                    Integer.valueOf(rt.color().substring(1, 3), 16),
                    Integer.valueOf(rt.color().substring(3, 5), 16),
                    Integer.valueOf(rt.color().substring(5, 7), 16));
            XSSFColor poiColor = new XSSFColor(awtColor, null);
            xf.setColor(poiColor);
          }

          // 拼接 marginRight（追加空格或全角空格）
          StringBuilder textWithMargin = new StringBuilder(rt.text());
          if (rt.marginRight() > 0) {
            for (int j = 0; j < rt.marginRight(); j++) {
              textWithMargin.append(" "); // 或 "\u2003" 全角空格
            }
          }

          rich.append(textWithMargin.toString(), xf);
        }

        cell.setCellValue(rich);

        // ========== 应用 header 样式（但不覆盖字体） ==========
        CellStyle baseStyle = wb.createCellStyle();
        baseStyle.cloneStyleFrom(styles.header.get(i));
        baseStyle.setFont(null); // ⚠️ 移除原字体，避免覆盖 RichText 的 XSSFFont
        cell.setCellStyle(baseStyle);

      } else {
        // 普通表头
        cell.setCellValue(col.name);
        cell.setCellStyle(styles.header.get(i));
      }

      // 设置列宽
      sheet.setColumnWidth(i, (int) ((cols.get(i).width + 0.72) * 256));
    }

    // === 合并 url 列表头 ===
    Map<String, List<Integer>> urlGroups = new HashMap<>();
    for (int i = 0; i < cols.size(); i++) {
      Col col = cols.get(i);
      if (col.isUrlExpanded) {
        urlGroups.computeIfAbsent(col.parentName, k -> new ArrayList<>()).add(i);
      }
    }
    for (Map.Entry<String, List<Integer>> e : urlGroups.entrySet()) {
      List<Integer> indexes = e.getValue();
      if (indexes.size() > 1) {
        sheet.addMergedRegion(
            new CellRangeAddress(0, 0, indexes.get(0), indexes.get(indexes.size() - 1)));
        Row headerRow = sheet.getRow(0);
        Cell mergedCell = headerRow.getCell(indexes.get(0));
        mergedCell.setCellValue(e.getKey()); // 父表头名
      }
    }

    // 4) 数据
    int r = 1;
    for (T rowObj : data) {
      Row row = sheet.createRow(r++);
      for (int i = 0; i < cols.size(); i++) {
        Cell cell = row.createCell(i);
        Col col = cols.get(i);
        Object raw = fieldValue(rowObj, col.field);
        String str = formatValue(raw, col, dictProvider);

        // 默认样式
        CellStyle cellStyle = styles.data.get(i);

        // === 动态颜色支持 ===
        String hexColor = resolveColor(raw, col, dictProvider);
        if (hexColor != null) {
          if (hexColor.startsWith("#")) {
            hexColor = hexColor.substring(1);
          }
          if (hexColor.length() == 6) { // 安全校验
            java.awt.Color awtColor =
                new java.awt.Color(
                    Integer.valueOf(hexColor.substring(0, 2), 16),
                    Integer.valueOf(hexColor.substring(2, 4), 16),
                    Integer.valueOf(hexColor.substring(4, 6), 16));
            XSSFColor poiColor = new XSSFColor(awtColor, null);
            CellStyle colored = wb.createCellStyle();
            colored.cloneStyleFrom(styles.data.get(i));
            ((XSSFCellStyle) colored).setFillForegroundColor(poiColor);
            colored.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cellStyle = colored; // 使用动态颜色 style
          }
        }

        // === 应用最终样式 ===
        cell.setCellStyle(cellStyle);

        // === URL 特殊逻辑 ===
        if (col.isUrlExpanded) {
          if (str != null && !str.trim().isEmpty()) {
            String[] urls = str.split("[,;\\s]+");
            if (col.urlIndex < urls.length) {
              String u = urls[col.urlIndex].trim();
              if (!u.isEmpty()) {
                // 设置超链接
                Hyperlink link = wb.getCreationHelper().createHyperlink(HyperlinkType.URL);
                link.setAddress(u);
                cell.setHyperlink(link);

                // 创建字体（蓝色+下划线）
                Font linkFont = wb.createFont();
                linkFont.setUnderline(Font.U_SINGLE);
                linkFont.setColor(IndexedColors.BLUE.getIndex());

                // 创建新的 style
                CellStyle urlStyle = wb.createCellStyle();
                urlStyle.cloneStyleFrom(cellStyle);
                urlStyle.setAlignment(HorizontalAlignment.CENTER);
                urlStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                urlStyle.setFont(linkFont); // ✅ 直接绑定字体

                cell.setCellStyle(urlStyle);

                // 设置单元格值
                cell.setCellValue("URL" + (col.urlIndex + 1));
              }
            }
          }
        } else {
          // 普通列
          cell.setCellValue(str);
        }
      }
    }

    int lastRow = sheet.getLastRowNum();
    int lastCol = cols.size() - 1;

    // === auto merge columns with @Xls(merge = true) ===
    for (int i = 0; i < cols.size(); i++) {
      if (cols.get(i).merge) {
        mergeSameRows(sheet, 1, lastRow, i); // 从第1行（数据行起始）到最后一行
      }
    }

    fillRemaining(wb, sheet, lastRow, lastCol);
  }

  private static class Styles {
    List<CellStyle> header;
    List<CellStyle> data;
  }

  private static Styles buildStyles(Workbook wb, List<Col> cols) {
    Styles s = new Styles();
    s.header = new ArrayList<>(cols.size());
    s.data = new ArrayList<>(cols.size());

    for (Col c : cols) {
      // header
      CellStyle h = wb.createCellStyle();
      h.setAlignment(HorizontalAlignment.CENTER);
      h.setVerticalAlignment(VerticalAlignment.CENTER);
      h.setFillPattern(FillPatternType.SOLID_FOREGROUND);
      h.setFillForegroundColor(c.headerBg.getIndex());
      setBorder(h);

      Font hf = wb.createFont();
      hf.setBold(c.headerBold);
      hf.setColor(c.headerFont.getIndex());
      hf.setFontName(c.headerFontFamily != null ? c.headerFontFamily : "Arial");
      hf.setFontHeightInPoints(c.headerFontSize);
      h.setFont(hf);

      // data
      CellStyle d = wb.createCellStyle();
      d.setAlignment(c.align);
      d.setVerticalAlignment(VerticalAlignment.CENTER);
      setBorder(d);
      Font df = wb.createFont();
      df.setFontName(c.fontFamily);
      df.setFontHeightInPoints(c.fontSize);
      df.setBold(c.bold);
      d.setFont(df);

      s.header.add(h);
      s.data.add(d);
    }
    return s;
  }

  private static void setBorder(CellStyle cs) {
    cs.setBorderBottom(BorderStyle.THIN);
    cs.setBorderTop(BorderStyle.THIN);
    cs.setBorderLeft(BorderStyle.THIN);
    cs.setBorderRight(BorderStyle.THIN);
    cs.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
    cs.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
    cs.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
    cs.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
  }

  /* ---------- 字段解析与值格式化 ---------- */
  private static class Col {
    Field field;
    String name;
    int width;
    int height; // 表头行高，默认 -1 表示不设置
    String dateFormat = ""; // ✅ 默认空字符串
    String converterExp = ""; // ✅ 默认空字符串
    String dictType = ""; // ✅ 默认空字符串
    HorizontalAlignment align;
    IndexedColors headerBg;
    IndexedColors headerFont;
    // extra config
    short fontSize;
    boolean bold;
    String fontFamily;
    short headerFontSize;
    boolean headerBold;
    String headerFontFamily;
    XlsRichText[] headerRichTexts;

    // new
    boolean merge;
    String type;

    // for url type
    boolean isUrl;
    boolean isUrlExpanded;
    String parentName;
    int urlIndex;
  }

  private static <T> List<Col> parseCols(Class<T> clazz, List<T> data) {
    List<Field> all = new ArrayList<>();
    if (clazz.getSuperclass() != null) {
      all.addAll(Arrays.asList(clazz.getSuperclass().getDeclaredFields()));
    }
    all.addAll(Arrays.asList(clazz.getDeclaredFields()));

    List<Col> cols = new ArrayList<>();

    for (Field f : all) {
      if (!f.isAnnotationPresent(Xls.class)) continue;
      Xls x = f.getAnnotation(Xls.class);

      if ("url".equalsIgnoreCase(x.type())) {
        // 找到最大 url 数量
        int maxUrlCount =
            data.stream()
                .mapToInt(
                    obj -> {
                      Object val = fieldValue(obj, f);
                      if (val == null) return 0;
                      return String.valueOf(val).split("[,;\\s]+").length;
                    })
                .max()
                .orElse(1);

        for (int i = 0; i < maxUrlCount; i++) {
          Col c = new Col();
          c.field = f;
          c.name = "URL" + (i + 1); // 子列头名称
          c.parentName = x.name(); // 父表头
          c.isUrl = true;
          c.isUrlExpanded = true;
          c.urlIndex = i;

          // 样式继承注解
          c.width = x.width();
          c.height = x.height();
          c.align = x.align();
          c.headerBg = x.headerBg();
          c.headerFont = x.headerFont();
          c.fontSize = x.fontSize();
          c.bold = x.bold();
          c.fontFamily = x.fontFamily();
          c.headerFontSize = x.headerFontSize();
          c.headerBold = x.headerBold();
          c.headerFontFamily = x.headerFontFamily();
          c.headerRichTexts = x.headerRichText();
          c.merge = x.merge();
          c.type = x.type();

          cols.add(c);
        }
      } else {
        Col c = new Col();
        c.field = f;
        c.name = x.name();
        c.width = x.width();
        c.height = x.height();
        c.dateFormat = x.dateFormat();
        c.converterExp = x.converterExp();
        c.dictType = x.dict();
        c.align = x.align();
        c.headerBg = x.headerBg();
        c.headerFont = x.headerFont();
        c.fontSize = x.fontSize();
        c.bold = x.bold();
        c.fontFamily = x.fontFamily();
        c.headerFontSize = x.headerFontSize();
        c.headerBold = x.headerBold();
        c.headerFontFamily = x.headerFontFamily();
        c.headerRichTexts = x.headerRichText();
        c.merge = x.merge();
        c.type = x.type();
        cols.add(c);
      }
    }

    return cols;
  }

  private static Object fieldValue(Object obj, Field f) {
    try {
      f.setAccessible(true);
      return f.get(obj);
    } catch (Exception e) {
      return null;
    }
  }

  private static String resolveColor(Object val, Col col, DictProvider dictProvider) {
    if (val == null) return null;
    if (!col.dictType.isEmpty() && dictProvider != null) {
      DictResult dr = dictProvider.getDict(col.dictType, String.valueOf(val));
      if (dr != null) {
        return dr.getColor();
      }
    }
    return null;
  }

  private static String formatValue(Object val, Col col, DictProvider dictProvider) {
    if (val == null) return "";
    String s = String.valueOf(val);

    // 1) 日期格式
    if (!col.dateFormat.isEmpty()) {
      if (val instanceof Date) {
        return new SimpleDateFormat(col.dateFormat).format((Date) val);
      }
      return s;
    }

    // 2) converterExp
    if (!col.converterExp.isEmpty()) {
      String mapped = mapByExp(s, col.converterExp);
      if (mapped != null) return mapped;
    }

    // 3) dict
    if (!col.dictType.isEmpty() && dictProvider != null) {
      DictResult dr = dictProvider.getDict(col.dictType, s);
      if (dr != null && dr.getLabel() != null) {
        return dr.getLabel();
      }
    }

    // 4) BigDecimal 去除多余小数
    if (val instanceof BigDecimal) {
      return ((BigDecimal) val).stripTrailingZeros().toPlainString();
    }

    return s;
  }

  private static String mapByExp(String value, String exp) {
    // 形如 "0=禁用,1=正常"
    String[] pairs = exp.split(",");
    for (String p : pairs) {
      String[] kv = p.split("=");
      if (kv.length == 2 && Objects.equals(kv[0], value)) {
        return kv[1];
      }
    }
    return null;
  }

  /* ---------- 合并行列 ---------- */

  /** Helper: safely get string value from cell */
  private static String getCellValue(Cell cell) {
    if (cell == null) return null;
    cell.setCellType(CellType.STRING);
    return cell.getStringCellValue();
  }

  /**
   * Merge rows if values are same in specified columns.
   *
   * @param sheet target sheet
   * @param startRow start row index (usually headerRow+1)
   * @param endRow last data row index
   * @param mergeCols column indexes to merge (e.g. [2] means merge "className" column)
   */
  private static void mergeSameRows(Sheet sheet, int startRow, int endRow, int... mergeCols) {
    for (int col : mergeCols) {
      int rowStart = startRow;
      String prev = getCellValue(sheet.getRow(rowStart).getCell(col));

      for (int row = startRow + 1; row <= endRow; row++) {
        String curr = getCellValue(sheet.getRow(row).getCell(col));
        if (!Objects.equals(curr, prev)) {
          // merge [rowStart..row-1]
          if (row - 1 > rowStart) {
            sheet.addMergedRegion(new CellRangeAddress(rowStart, row - 1, col, col));
          }
          rowStart = row;
          prev = curr;
        }
      }
      // 最后一段
      if (endRow > rowStart) {
        sheet.addMergedRegion(new CellRangeAddress(rowStart, endRow, col, col));
      }
    }
  }

  private static void fillRemaining(Workbook wb, Sheet sheet, int lastDataRow, int lastDataCol) {
    // 我们限定 200 行 × 50 列 的区域，避免 SXSSF 崩溃
    int targetRow = lastDataRow + 1000; // 向下扩展 200 行
    int targetCol = lastDataCol + 1000; // 向右扩展 50 列

    // 白色背景样式（无边框）
    CellStyle whiteStyle = wb.createCellStyle();
    whiteStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
    whiteStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

    // 遍历扩展范围内的所有单元格
    for (int r = 0; r <= targetRow; r++) {
      Row row = sheet.getRow(r);
      if (row == null) {
        row = sheet.createRow(r);
      }
      for (int c = 0; c <= targetCol; c++) {
        // 已有数据的区域跳过
        if (r <= lastDataRow && c <= lastDataCol) {
          continue;
        }
        Cell cell = row.getCell(c);
        if (cell == null) {
          cell = row.createCell(c);
        }
        cell.setCellStyle(whiteStyle);
      }
    }
  }
}
