package com.api.common.controller;

import java.beans.PropertyEditorSupport;
import java.util.Date;
import java.util.List;

import com.api.common.constant.HttpStatus;
import com.api.common.domain.AjaxResult;
import com.api.common.domain.entity.LoginUser;
import com.api.common.utils.*;
import com.api.common.utils.pagination.PageDomain;
import com.api.common.utils.pagination.TableDataInfo;
import com.api.common.utils.pagination.TableSupport;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;




import lombok.extern.slf4j.Slf4j;

/**
 * Base controller providing common web-layer utilities.
 * <p>
 * Responsibilities:
 * - Automatically convert request parameters (e.g., Date parsing).
 * - Handle pagination and sorting with PageHelper.
 * - Standardize response objects (AjaxResult, TableDataInfo).
 * - Provide user-related helpers (ID, department, username).
 * - Common redirect and result conversion methods.
 * </p>
 *
 * @author ruoyi
 */
@Slf4j
public class BaseController {

    /**
     * Register a binder to convert request strings into {@link Date}.
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(DateUtils.parseDate(text));
            }
        });
    }

    /**
     * Initialize pagination using PageHelper.
     */
    protected void startPage() {
        PageUtils.startPage();
    }

    /**
     * Initialize sorting configuration.
     */
    protected void startOrderBy() {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        if (StringUtils.isNotEmpty(pageDomain.getOrderBy())) {
            String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
            PageHelper.orderBy(orderBy);
        }
    }

    /**
     * Clear thread-local pagination variables.
     */
    protected void clearPage() {
        PageUtils.clearPage();
    }

    /**
     * Build a paginated response for table data.
     *
     * @param list data list
     * @return table response
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected TableDataInfo getDataTable(List<?> list) {
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setMsg("Query successful");
        rspData.setRows(list);
        rspData.setTotal(new PageInfo(list).getTotal());
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

    /**
     * Perform a redirect to a given URL.
     */
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
