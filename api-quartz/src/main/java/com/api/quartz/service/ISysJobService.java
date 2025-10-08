package com.api.quartz.service;

import com.api.common.exceptions.TaskException;
import com.api.quartz.domain.SysJob;
import org.quartz.SchedulerException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for managing scheduled jobs using Quartz.
 *
 * <p>定时任务调度信息服务层接口 — 提供创建、修改、暂停、删除等任务管理功能。
 *
 * @author Gavin
 */
public interface ISysJobService {

  /**
   * Retrieves all scheduled jobs matching given criteria.
   *
   * @param job filter conditions
   * @return list of scheduled jobs
   */
  List<SysJob> selectJobList(SysJob job);

  /**
   * Retrieves a paginated list of scheduled jobs matching given criteria.
   *
   * @param job
   * @param pageable
   * @return
   */
  Page<SysJob> selectJobList(SysJob job, Pageable pageable);

  /**
   * Retrieves job details by its ID.
   *
   * @param jobId job ID
   * @return job details, or {@code null} if not found
   */
  SysJob selectJobById(Long jobId);

  /**
   * Pauses a scheduled job.
   *
   * @param job job to pause
   * @return update result (1 = success)
   * @throws SchedulerException when Quartz scheduler fails
   */
  int pauseJob(SysJob job) throws SchedulerException;

  /**
   * Resumes a paused job.
   *
   * @param job job to resume
   * @return update result (1 = success)
   * @throws SchedulerException when Quartz scheduler fails
   */
  int resumeJob(SysJob job) throws SchedulerException;

  /**
   * Deletes a scheduled job and its trigger.
   *
   * @param job job to delete
   * @return delete result (1 = success)
   * @throws SchedulerException when Quartz scheduler fails
   */
  int deleteJob(SysJob job) throws SchedulerException;

  /**
   * Deletes multiple scheduled jobs by IDs.
   *
   * @param jobIds IDs of jobs to delete
   * @throws SchedulerException when Quartz scheduler fails
   */
  void deleteJobByIds(Long[] jobIds) throws SchedulerException;

  /**
   * Updates the status (pause/resume) of a job.
   *
   * @param job job with updated status
   * @return update result
   * @throws SchedulerException when Quartz scheduler fails
   */
  int changeStatus(SysJob job) throws SchedulerException;

  /**
   * Triggers a job to run immediately.
   *
   * @param job job to trigger
   * @return {@code true} if executed successfully
   * @throws SchedulerException when Quartz scheduler fails
   */
  boolean run(SysJob job) throws SchedulerException;

  /**
   * Adds a new scheduled job.
   *
   * @param job job details
   * @return insert result (1 = success)
   * @throws SchedulerException when Quartz scheduler fails
   * @throws TaskException when job configuration is invalid
   */
  int insertJob(SysJob job) throws SchedulerException, TaskException;

  /**
   * Updates an existing job’s scheduling details.
   *
   * @param job updated job details
   * @return update result (1 = success)
   * @throws SchedulerException when Quartz scheduler fails
   * @throws TaskException when job configuration is invalid
   */
  int updateJob(SysJob job) throws SchedulerException, TaskException;

  /**
   * Validates a given cron expression.
   *
   * @param cronExpression cron expression string
   * @return {@code true} if valid, otherwise {@code false}
   */
  boolean checkCronExpressionIsValid(String cronExpression);
}
