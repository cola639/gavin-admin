package com.api.common.utils.pagination;

import org.springframework.data.domain.Page;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Generic table pagination response object.
 *
 * <p>Compatible with RuoYi front-end format, but supports type-safety and JPA Page objects.
 *
 * @author
 */
public class TableDataInfo<T> implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /** Status code (0 = success) */
  private int code = 0;

  /** Message */
  private String msg = "Query successful";

  /** Total record count */
  private long total;

  /** Data rows */
  private List<T> rows;

  // --------------------------------------------------------
  // Constructors
  // --------------------------------------------------------

  public TableDataInfo() {}

  public TableDataInfo(List<T> rows, long total) {
    this.rows = rows;
    this.total = total;
  }

  public TableDataInfo(List<T> rows, long total, int code, String msg) {
    this.rows = rows;
    this.total = total;
    this.code = code;
    this.msg = msg;
  }

  // --------------------------------------------------------
  // Static factory methods
  // --------------------------------------------------------

  /** ✅ Create from Spring Data Page */
  public static <T> TableDataInfo<T> of(Page<T> page) {
    return new TableDataInfo<>(page.getContent(), page.getTotalElements());
  }

  /** ✅ Create from list */
  public static <T> TableDataInfo<T> of(List<T> list, long total) {
    return new TableDataInfo<>(list, total);
  }

  /** ✅ Create success result */
  public static <T> TableDataInfo<T> success(Page<T> page) {
    return new TableDataInfo<>(page.getContent(), page.getTotalElements(), 0, "Query successful");
  }

  /** ✅ Create error result */
  public static <T> TableDataInfo<T> error(String msg) {
    return new TableDataInfo<>(List.of(), 0, 500, msg);
  }

  // --------------------------------------------------------
  // Getters & Setters
  // --------------------------------------------------------

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public long getTotal() {
    return total;
  }

  public void setTotal(long total) {
    this.total = total;
  }

  public List<T> getRows() {
    return rows;
  }

  public void setRows(List<T> rows) {
    this.rows = rows;
  }
}
