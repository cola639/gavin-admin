package controller;

import com.api.common.controller.BaseController;
import com.api.common.domain.AjaxResult;
import com.api.common.utils.pagination.TableDataInfo;
import domain.SysJobLog;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import service.ISysJobLogService;

import java.util.List;

/**
 * REST Controller for handling Quartz job execution logs.
 *
 * <p>Responsibilities: - Retrieve and export job logs. - View detailed log entries. - Delete or
 * clean job log history.
 *
 * <p>Design principles: - Uses @Slf4j for structured logging. - Uses constructor-based dependency
 * injection (@Autowired). - Returns standardized AjaxResult and TableDataInfo responses. - Fully
 * compatible with Java 17 and Spring Boot 3.5.
 */
@Slf4j
@RestController
@RequestMapping("/monitor/jobLog")
public class SysJobLogController extends BaseController {

  private final ISysJobLogService jobLogService;

  @Autowired
  public SysJobLogController(ISysJobLogService jobLogService) {
    this.jobLogService = jobLogService;
  }

  /**
   * Retrieves a paginated list of scheduled job execution logs.
   *
   * @param sysJobLog filtering criteria for job logs
   * @return paginated list of job logs
   */
  @PreAuthorize("@ss.hasPermi('monitor:job:list')")
  @GetMapping("/list")
  public TableDataInfo list(SysJobLog sysJobLog) {}

  /**
   * Retrieves details for a specific job log by ID.
   *
   * @param jobLogId the ID of the job log to retrieve
   * @return job log details wrapped in AjaxResult
   */
  @PreAuthorize("@ss.hasPermi('monitor:job:query')")
  @GetMapping("/{jobLogId}")
  public AjaxResult getInfo(@PathVariable Long jobLogId) {
    log.debug("Fetching details for job log ID: {}", jobLogId);
    SysJobLog jobLog = jobLogService.selectJobLogById(jobLogId);
    return success(jobLog);
  }

  /**
   * Deletes one or more scheduled job logs by their IDs.
   *
   * @param jobLogIds array of job log IDs to delete
   * @return result of deletion operation
   */
  @PreAuthorize("@ss.hasPermi('monitor:job:remove')")
  @DeleteMapping("/{jobLogIds}")
  public AjaxResult remove(@PathVariable Long[] jobLogIds) {
    log.warn("Deleting job logs with IDs: {}", (Object) jobLogIds);
    int deleted = jobLogService.deleteJobLogByIds(jobLogIds);
    return toAjax(deleted);
  }

  /**
   * Clears all scheduled job logs from the system.
   *
   * @return AjaxResult indicating success
   */
  @DeleteMapping("/clean")
  public AjaxResult clean() {
    log.warn("Cleaning all scheduled job logs...");
    jobLogService.cleanJobLog();
    return success("All job logs have been successfully cleaned.");
  }
}
