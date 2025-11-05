package com.api.common.utils.pagination;

import com.api.common.constant.HttpStatus;
import org.springframework.data.domain.Page;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Generic table pagination response object.
 *
 * <p>Consistent with AjaxResult, using standard HttpStatus codes.
 *
 * @author
 */
public class TableDataInfo<T> implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /** Status code (see {@link HttpStatus}) */
  private int code = HttpStatus.SUCCESS;

  /** Message */
  private String msg = "Query successful";

  /** Total record count */
  private long total;

  /** Data rows */
  private List<T> rows = Collections.emptyList();

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

  /** ✅ Create from Spring Data Page (Success) */
  //  public static <T> TableDataInfo<T> of(Page<T> page) {
  //    return new TableDataInfo<>(
  //        page.getContent(), page.getTotalElements(), HttpStatus.SUCCESS, "Query successful");
  //  }

  /** ✅ Create from list and total (Success) */
  //  public static <T> TableDataInfo<T> of(List<T> list, long total) {
  //    return new TableDataInfo<>(list, total, HttpStatus.SUCCESS, "Query successful");
  //  }

  /** ✅ Success result with custom message */
  public static <T> TableDataInfo<T> success(String msg, List<T> list, long total) {
    return new TableDataInfo<>(list, total, HttpStatus.SUCCESS, msg);
  }

  /** ✅ Success result from Page */
  public static <T> TableDataInfo<T> success(Page<T> page) {
    return new TableDataInfo<>(
        page.getContent(), page.getTotalElements(), HttpStatus.SUCCESS, "Query successful");
  }

  /** ⚠️ Warning result */
  public static <T> TableDataInfo<T> warn(String msg, List<T> list) {
    return new TableDataInfo<>(list, list.size(), HttpStatus.WARN, msg);
  }

  /** ❌ Error result */
  public static <T> TableDataInfo<T> error(String msg) {
    return new TableDataInfo<>(Collections.emptyList(), 0, HttpStatus.ERROR, msg);
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
