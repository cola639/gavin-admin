package com.api.quartz.service;

import com.api.quartz.domain.SysJobLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.api.quartz.repository.SysJobLogRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysJobLogServiceImpl implements ISysJobLogService {

  private final SysJobLogRepository jobLogRepository;

  @Override
  public List<SysJobLog> selectJobLogList(SysJobLog jobLog) {
    // Basic implementation — can extend with Specification for filtering
    return jobLogRepository.findAll();
  }

  @Override
  public SysJobLog selectJobLogById(Long jobLogId) {
    return jobLogRepository.findById(jobLogId).orElse(null);
  }

  @Override
  public void addJobLog(SysJobLog jobLog) {
    jobLogRepository.save(jobLog);
    log.info("Added new job log for {}", jobLog.getJobName());
  }

  @Override
  public int deleteJobLogByIds(Long[] logIds) {
    jobLogRepository.deleteAllById(List.of(logIds));
    return logIds.length;
  }

  @Override
  public int deleteJobLogById(Long jobId) {
    jobLogRepository.deleteById(jobId);
    return 1;
  }

  @Override
  public void cleanJobLog() {
    jobLogRepository.deleteAll();
    log.info("Cleared all job logs.");
  }
}
