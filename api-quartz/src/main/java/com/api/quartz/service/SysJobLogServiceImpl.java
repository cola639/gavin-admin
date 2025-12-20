package com.api.quartz.service;

import com.api.common.utils.jpa.SpecificationBuilder;
import com.api.quartz.domain.SysJobLog;
import com.api.quartz.repository.SysJobLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysJobLogServiceImpl implements ISysJobLogService {

  private final SysJobLogRepository jobLogRepository;

  @Override
  public Page<SysJobLog> selectJobLogPage(
      SysJobLog filter, Map<String, Object> params, Pageable pageable) {
    SysJobLog criteria = (filter != null) ? filter : new SysJobLog();

    Date beginTime = params != null ? (Date) params.get("beginTime") : null;
    Date endTime = params != null ? (Date) params.get("endTime") : null;

    Specification<SysJobLog> spec =
        SpecificationBuilder.<SysJobLog>builder()
            .eq("jobLogId", criteria.getJobLogId())
            .like("jobName", criteria.getJobName())
            .eq("jobGroup", criteria.getJobGroup())
            .like("invokeTarget", criteria.getInvokeTarget())
            .eq("status", criteria.getStatus())
            .between("createTime", beginTime, endTime);

    if (pageable == null || pageable.isUnpaged()) {
      List<SysJobLog> list =
          jobLogRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createTime"));
      return new PageImpl<>(list, Pageable.unpaged(), list.size());
    }

    return jobLogRepository.findAll(spec, pageable);
  }

  @Override
  public List<SysJobLog> selectJobLogList(SysJobLog filter) {
    return selectJobLogPage(filter, Map.of(), Pageable.unpaged()).getContent();
  }

  @Override
  public SysJobLog selectJobLogById(Long jobLogId) {
    return jobLogRepository.findById(jobLogId).orElse(null);
  }

  @Override
  @Transactional
  public void addJobLog(SysJobLog jobLog) {
    jobLogRepository.save(jobLog);
  }

  @Override
  @Transactional
  public int deleteJobLogByIds(Long[] logIds) {
    if (logIds == null || logIds.length == 0) {
      return 0;
    }
    jobLogRepository.deleteAllByIdInBatch(Arrays.asList(logIds));
    return logIds.length;
  }

  @Override
  @Transactional
  public int deleteJobLogById(Long jobId) {
    if (jobId == null) return 0;
    if (!jobLogRepository.existsById(jobId)) return 0;
    jobLogRepository.deleteById(jobId);
    return 1;
  }

  @Override
  @Transactional
  public void cleanJobLog() {
    jobLogRepository.deleteAllInBatch();
  }
}
