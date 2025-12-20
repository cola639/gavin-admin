package com.api.quartz.service;

import com.api.quartz.domain.SysJobLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface ISysJobLogService {

  Page<SysJobLog> selectJobLogPage(SysJobLog filter, Map<String, Object> params, Pageable pageable);

  List<SysJobLog> selectJobLogList(SysJobLog jobLog);

  SysJobLog selectJobLogById(Long jobLogId);

  void addJobLog(SysJobLog jobLog);

  int deleteJobLogByIds(Long[] logIds);

  int deleteJobLogById(Long jobId);

  void cleanJobLog();
}
