package com.api.boot.controller.quartz;

import com.api.common.controller.BaseController;
import com.api.common.domain.AjaxResult;
import com.api.common.utils.pagination.TableDataInfo;
import com.api.quartz.domain.SysJobLog;
import com.api.quartz.service.ISysJobLogService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/monitor/jobLog")
public class SysJobLogController extends BaseController {

  private final ISysJobLogService jobLogService;

  @PreAuthorize("@ss.hasPermi('monitor:job:list')")
  @GetMapping("/list")
  public TableDataInfo<SysJobLog> list(
      SysJobLog filter,
      @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
      @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
      @RequestParam(value = "unpaged", defaultValue = "false") boolean unpaged,
      @RequestParam(value = "beginTime", required = false)
          @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
          Date beginTime,
      @RequestParam(value = "endTime", required = false)
          @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
          Date endTime) {

    Map<String, Object> params = new HashMap<>();
    params.put("beginTime", beginTime);
    params.put("endTime", endTime);

    Pageable pageable =
        unpaged
            ? Pageable.unpaged()
            : PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));

    Page<SysJobLog> page = jobLogService.selectJobLogPage(filter, params, pageable);
    return TableDataInfo.success(page);
  }

  @PreAuthorize("@ss.hasPermi('monitor:job:query')")
  @GetMapping("/{jobLogId}")
  public AjaxResult getInfo(@PathVariable @NotNull Long jobLogId) {
    SysJobLog jobLog = jobLogService.selectJobLogById(jobLogId);
    return success(jobLog);
  }

  @PreAuthorize("@ss.hasPermi('monitor:job:remove')")
  @DeleteMapping("/{jobLogIds}")
  public AjaxResult remove(@PathVariable Long[] jobLogIds) {
    int deleted = jobLogService.deleteJobLogByIds(jobLogIds);
    return toAjax(deleted);
  }

  @PreAuthorize("@ss.hasPermi('monitor:job:remove')")
  @DeleteMapping("/clean")
  public AjaxResult clean() {
    jobLogService.cleanJobLog();
    return success("All job logs have been successfully cleaned.");
  }
}
