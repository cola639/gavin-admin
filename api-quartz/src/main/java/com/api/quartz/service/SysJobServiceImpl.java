package com.api.quartz.service;

import com.api.common.exceptions.TaskException;
import com.api.common.utils.jpa.SpecificationBuilder;
import com.api.quartz.constant.ScheduleConstants;
import com.api.quartz.domain.SysJob;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.api.quartz.repository.SysJobRepository;
import com.api.quartz.util.CronUtils;
import com.api.quartz.util.ScheduleUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysJobServiceImpl implements ISysJobService {

  private final Scheduler scheduler;
  private final SysJobRepository jobRepository;

  // Initialize the scheduler on application startup
  @PostConstruct
  public void init() throws SchedulerException, TaskException {
    scheduler.clear();
    List<SysJob> jobList = jobRepository.findAll();
    for (SysJob job : jobList) {
      ScheduleUtils.createScheduleJob(scheduler, job);
    }
    log.info("Quartz Scheduler initialized with {} jobs.", jobList.size());
  }

  @Override
  public List<SysJob> selectJobList(SysJob job) {
    return jobRepository.findAll();
  }

  @Override
  public Page<SysJob> selectJobList(SysJob job, Pageable pageable) {
    Specification<SysJob> spec =
        SpecificationBuilder.<SysJob>builder()
            .eq("jobId", job.getJobId())
            .like("jobName", job.getJobName())
            .eq("jobGroup", job.getJobGroup())
            .eq("status", job.getStatus());

    return jobRepository.findAll(spec, pageable);
  }

  @Override
  public SysJob selectJobById(Long jobId) {
    return jobRepository.findById(jobId).orElse(null);
  }

  @Override
  @Transactional
  public int pauseJob(SysJob job) throws SchedulerException {
    job.setStatus(ScheduleConstants.Status.PAUSE.getValue());
    jobRepository.save(job);
    scheduler.pauseJob(ScheduleUtils.getJobKey(job.getJobId(), job.getJobGroup()));
    log.info("Paused job: {}", job.getJobName());
    return 1;
  }

  @Override
  @Transactional
  public int resumeJob(SysJob job) throws SchedulerException {
    job.setStatus(ScheduleConstants.Status.NORMAL.getValue());
    jobRepository.save(job);
    scheduler.resumeJob(ScheduleUtils.getJobKey(job.getJobId(), job.getJobGroup()));
    log.info("Resumed job: {}", job.getJobName());
    return 1;
  }

  @Override
  @Transactional
  public int deleteJob(SysJob job) throws SchedulerException {
    jobRepository.deleteById(job.getJobId());
    scheduler.deleteJob(ScheduleUtils.getJobKey(job.getJobId(), job.getJobGroup()));
    log.info("Deleted job: {}", job.getJobName());
    return 1;
  }

  @Override
  @Transactional
  public void deleteJobByIds(Long[] jobIds) throws SchedulerException {
    for (Long id : jobIds) {
      SysJob job = jobRepository.findById(id).orElse(null);
      if (job != null) deleteJob(job);
    }
  }

  @Override
  @Transactional
  public int changeStatus(SysJob job) throws SchedulerException {
    if (ScheduleConstants.Status.NORMAL.getValue().equals(job.getStatus())) {
      return resumeJob(job);
    } else {
      return pauseJob(job);
    }
  }

  @Override
  @Transactional
  public boolean run(SysJob job) throws SchedulerException {
    SysJob properties = selectJobById(job.getJobId());

    // Parameters
    JobDataMap dataMap = new JobDataMap();
    dataMap.put(ScheduleConstants.TASK_PROPERTIES, properties);
    JobKey jobKey = ScheduleUtils.getJobKey(job.getJobId(), job.getJobGroup());
    if (scheduler.checkExists(jobKey)) {
      scheduler.triggerJob(jobKey, dataMap);
      log.info("Triggered job: {}", job.getJobName());
      return true;
    }
    return false;
  }

  @Override
  @Transactional
  public int insertJob(SysJob job) throws SchedulerException, TaskException {
    job.setStatus(ScheduleConstants.Status.PAUSE.getValue());
    SysJob saved = jobRepository.save(job);
    ScheduleUtils.createScheduleJob(scheduler, saved);
    log.info("Created new job: {}", saved.getJobName());
    return 1;
  }

  @Override
  @Transactional
  public int updateJob(SysJob job) throws SchedulerException, TaskException {
    SysJob existing = jobRepository.findById(job.getJobId()).orElse(null);
    jobRepository.save(job);
    if (existing != null) {
      updateSchedulerJob(job, existing.getJobGroup());
    }
    return 1;
  }

  // Update the scheduled job in Quartz
  public void updateSchedulerJob(SysJob job, String jobGroup)
      throws SchedulerException, TaskException {
    JobKey jobKey = ScheduleUtils.getJobKey(job.getJobId(), jobGroup);
    // Remove existing job
    if (scheduler.checkExists(jobKey)) {
      scheduler.deleteJob(jobKey);
    }
    // Recreate job with updated details
    ScheduleUtils.createScheduleJob(scheduler, job);
  }

  @Override
  public boolean checkCronExpressionIsValid(String cronExpression) {
    return CronUtils.isValid(cronExpression);
  }
}
