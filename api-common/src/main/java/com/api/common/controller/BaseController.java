package com.api.common.controller;

import java.util.Date;
import java.util.List;

import com.api.common.constant.HttpStatus;
import com.api.common.domain.AjaxResult;
import com.api.common.domain.LoginUser;
import com.api.common.utils.SecurityUtils;
import com.api.common.utils.StringUtils;
import com.api.common.utils.pagination.TableDataInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;

/**
 * Base controller providing common web-layer utilities.
 *
 * <p>Responsibilities: - Automatically convert request parameters (e.g., Date parsing). -
 * Standardize response objects (AjaxResult, TableDataInfo). - Provide user-related helpers (ID,
 * department, username). - Common redirect and result conversion methods.
 */
@Slf4j
public class BaseController {

  /** Register a binder to convert request strings into {@link Date}. */
  //    @InitBinder
  //    public void initBinder(WebDataBinder binder) {
  //        binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
  //            @Override
  //            public void setAsText(String text) {
  //                setValue(DateUtils.parseDate(text));
  //            }
  //        });
  //    }

  /**
   * Build a paginated response from Spring Data JPA Page object.
   *
   * @param page Page object
   * @return table response
   */
  protected <T> TableDataInfo getDataTable(Page<T> page) {
    TableDataInfo rspData = new TableDataInfo();
    rspData.setCode(HttpStatus.SUCCESS);
    rspData.setMsg("Query successful");
    rspData.setRows(page.getContent());
    rspData.setTotal(page.getTotalElements());
    return rspData;
  }

  // Overload for non-paginated lists, no store in database
  protected <T> TableDataInfo getDataTable(List<T> list) {
    TableDataInfo rspData = new TableDataInfo();
    rspData.setCode(HttpStatus.SUCCESS);
    rspData.setMsg("Query successful");
    rspData.setRows(list);
    rspData.setTotal(list.size());
    return rspData;
  }

  /** Build a paginated response manually (e.g., for Redis metrics or in-memory lists). */
  protected <T> TableDataInfo getDataTable(List<T> list, long total) {
    TableDataInfo rspData = new TableDataInfo();
    rspData.setCode(HttpStatus.SUCCESS);
    rspData.setMsg("Query successful");
    rspData.setRows(list);
    rspData.setTotal(total);
    return rspData;
  }

  /** ----------- Response Wrappers ----------- */
  public AjaxResult success() {
    return AjaxResult.success();
  }

  public AjaxResult success(String message) {
    return AjaxResult.success(message);
  }

  public AjaxResult success(Object data) {
    return AjaxResult.success(data);
  }

  public AjaxResult error() {
    return AjaxResult.error();
  }

  public AjaxResult error(String message) {
    return AjaxResult.error(message);
  }

  public AjaxResult warn(String message) {
    return AjaxResult.warn(message);
  }

  /**
   * Convert operation result to AjaxResult.
   *
   * @param rows number of affected rows
   */
  protected AjaxResult toAjax(int rows) {
    return rows > 0 ? success() : error();
  }

  /**
   * Convert boolean result to AjaxResult.
   *
   * @param result operation result
   */
  protected AjaxResult toAjax(boolean result) {
    return result ? success() : error();
  }

  /** Perform a redirect to a given URL. */
  public String redirect(String url) {
    return StringUtils.format("redirect:{}", url);
  }

  /** ----------- User Information Helpers ----------- */
  public LoginUser getLoginUser() {
    return SecurityUtils.getLoginUser();
  }

  public Long getUserId() {
    return getLoginUser().getUserId();
  }

  public Long getDeptId() {
    return getLoginUser().getDeptId();
  }

  public String getUsername() {
    return getLoginUser().getUsername();
  }
}
