// package controller;
//
// import com.api.common.constant.Constants;
// import com.api.common.controller.BaseController;
// import com.api.common.domain.AjaxResult;
// import com.api.common.exceptions.TaskException;
// import com.api.common.utils.StringUtils;
// import com.api.common.utils.pagination.TableDataInfo;
// import com.api.persistence.domain.common.SysUser;
// import domain.SysJob;
// import jakarta.servlet.http.HttpServletResponse;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.quartz.SchedulerException;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.web.bind.annotation.*;
// import service.ISysJobService;
// import util.CronUtils;
// import util.ScheduleUtils;
//
// import java.util.List;
//
/// **
// * REST Controller for managing Quartz scheduled jobs.
// *
// * <p>Provides CRUD operations and runtime controls for system tasks.
// *
// * <p>Design principles: - Constructor-based dependency injection (@Autowired) - Uses @Slf4j for
// * logging - Clean separation of validation, logging, and service delegation - Compatible with
// Java
// * 17 and Spring Boot 3.5
// */
// @Slf4j
// @RequiredArgsConstructor
// @RestController
// @RequestMapping("/monitor/job")
// public class SysJobController extends BaseController {
//
//  private final ISysJobService jobService;
//
//  /** Retrieves a paginated list of scheduled jobs. */
//  @GetMapping("/list")
//  public TableDataInfo list(SysJob sysJob) {
//    Pageable pageable = PageRequest.of(0, 10);
//    Page<SysJob> page = jobService.selectJobList(sysJob, pageable);
//
//    return getDataTable(page);
//  }
//
//  /** Retrieves detailed job information by jobId. */
//  @GetMapping("/{jobId}")
//  public AjaxResult getInfo(@PathVariable Long jobId) {
//    log.debug("Fetching job details for jobId={}", jobId);
//    return success(jobService.selectJobById(jobId));
//  }
//
//  /** Creates a new scheduled job. */
//  @PostMapping
//  public AjaxResult add(@RequestBody SysJob job) throws SchedulerException, TaskException {
//    log.info("Creating new scheduled job: {}", job.getJobName());
//
//    String validationError = validateJob(job);
//    if (validationError != null) {
//      log.warn("Job validation failed: {}", validationError);
//      return error(validationError);
//    }
//
//    job.setCreateBy(getUsername());
//    return toAjax(jobService.insertJob(job));
//  }
//
//  /** Updates an existing scheduled job. */
//  @PreAuthorize("@ss.hasPermi('monitor:job:edit')")
//  @PutMapping
//  public AjaxResult edit(@RequestBody SysJob job) throws SchedulerException, TaskException {
//    log.info("Updating scheduled job: {}", job.getJobName());
//
//    String validationError = validateJob(job);
//    if (validationError != null) {
//      log.warn("Job update failed: {}", validationError);
//      return error(validationError);
//    }
//
//    job.setUpdateBy(getUsername());
//    return toAjax(jobService.updateJob(job));
//  }
//
//  /** Changes the status of a job (e.g., enable/disable). */
//  @PutMapping("/changeStatus")
//  public AjaxResult changeStatus(@RequestBody SysJob job) throws SchedulerException {
//    log.info("Changing job status: jobId={}, newStatus={}", job.getJobId(), job.getStatus());
//    SysJob existingJob = jobService.selectJobById(job.getJobId());
//    existingJob.setStatus(job.getStatus());
//    return toAjax(jobService.changeStatus(existingJob));
//  }
//
//  /** Executes a job immediately (run once). */
//  @PutMapping("/run")
//  public AjaxResult run(@RequestBody SysJob job) throws SchedulerException {
//    log.info("Running job immediately: {}", job.getJobName());
//    boolean result = jobService.run(job);
//    return result ? success("Job executed successfully.") : error("Job not found or expired.");
//  }
//
//  /** Deletes one or more scheduled jobs. */
//  @DeleteMapping("/{jobIds}")
//  public AjaxResult remove(@PathVariable Long[] jobIds) throws SchedulerException {
//    log.warn("Deleting scheduled jobs: {}", (Object) jobIds);
//    jobService.deleteJobByIds(jobIds);
//    return success("Selected jobs deleted successfully.");
//  }
//
//  /**
//   * Validates job configuration against security and format rules.
//   *
//   * @param job SysJob object to validate
//   * @return error message if invalid, or null if valid
//   */
//  private String validateJob(SysJob job) {
//    String jobName = job.getJobName();
//
//    if (!CronUtils.isValid(job.getCronExpression())) {
//      return String.format("Failed to save job '%s': invalid Cron expression.", jobName);
//    }
//
//    String target = job.getInvokeTarget();
//    if (StringUtils.containsIgnoreCase(target, Constants.LOOKUP_RMI)) {
//      return String.format("Failed to save job '%s': RMI calls are not allowed.", jobName);
//    }
//    if (StringUtils.containsAnyIgnoreCase(target, Constants.LOOKUP_LDAP, Constants.LOOKUP_LDAPS))
// {
//      return String.format("Failed to save job '%s': LDAP calls are not allowed.", jobName);
//    }
//    if (StringUtils.containsAnyIgnoreCase(target, Constants.HTTP, Constants.HTTPS)) {
//      return String.format("Failed to save job '%s': HTTP(S) calls are not allowed.", jobName);
//    }
//    if (StringUtils.containsAnyIgnoreCase(target, Constants.JOB_ERROR_LIST)) {
//      return String.format("Failed to save job '%s': illegal expression detected.", jobName);
//    }
//    if (!ScheduleUtils.whiteList(target)) {
//      return String.format("Failed to save job '%s': target not in whitelist.", jobName);
//    }
//
//    return null;
//  }
// }
