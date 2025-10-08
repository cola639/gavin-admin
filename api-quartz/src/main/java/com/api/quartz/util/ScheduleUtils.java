package com.api.quartz.util;

import com.api.common.constant.Constants;
import com.api.common.exceptions.TaskException;
import com.api.common.utils.StringUtils;
import com.api.common.utils.springUtils.SpringUtils;
import com.api.quartz.constant.ScheduleConstants;
import com.api.quartz.domain.SysJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;

/** Utility class for managing Quartz job scheduling. */
@Slf4j
public final class ScheduleUtils {

  private ScheduleUtils() {}

  /** Determines which job class to use (concurrent or not). */
  private static Class<? extends Job> getQuartzJobClass(SysJob job) {
    return "0".equals(job.getConcurrent())
        ? QuartzJobExecution.class
        : QuartzDisallowConcurrentExecution.class;
  }

  public static TriggerKey getTriggerKey(Long jobId, String group) {
    return TriggerKey.triggerKey(ScheduleConstants.TASK_CLASS_NAME + jobId, group);
  }

  public static JobKey getJobKey(Long jobId, String group) {
    return JobKey.jobKey(ScheduleConstants.TASK_CLASS_NAME + jobId, group);
  }

  /** Creates or replaces a scheduled job. */
  public static void createScheduleJob(Scheduler scheduler, SysJob job)
      throws SchedulerException, TaskException {

    Class<? extends Job> jobClass = getQuartzJobClass(job);
    JobDetail jobDetail =
        JobBuilder.newJob(jobClass)
            .withIdentity(getJobKey(job.getJobId(), job.getJobGroup()))
            .build();

    CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());
    scheduleBuilder = applyMisfirePolicy(job, scheduleBuilder);

    CronTrigger trigger =
        TriggerBuilder.newTrigger()
            .withIdentity(getTriggerKey(job.getJobId(), job.getJobGroup()))
            .withSchedule(scheduleBuilder)
            .build();

    jobDetail.getJobDataMap().put(ScheduleConstants.TASK_PROPERTIES, job);

    if (scheduler.checkExists(getJobKey(job.getJobId(), job.getJobGroup()))) {
      scheduler.deleteJob(getJobKey(job.getJobId(), job.getJobGroup()));
    }

    if (CronUtils.getNextExecution(job.getCronExpression()) != null) {
      scheduler.scheduleJob(jobDetail, trigger);
      log.info("Scheduled job '{}'", job.getJobName());
    }

    if (ScheduleConstants.Status.PAUSE.getValue().equals(job.getStatus())) {
      scheduler.pauseJob(getJobKey(job.getJobId(), job.getJobGroup()));
      log.debug("Job '{}' created in paused state.", job.getJobName());
    }
  }

  /** Applies misfire policy to a cron trigger. */
  private static CronScheduleBuilder applyMisfirePolicy(SysJob job, CronScheduleBuilder cb)
      throws TaskException {
    return switch (job.getMisfirePolicy()) {
      case ScheduleConstants.MISFIRE_DEFAULT -> cb;
      case ScheduleConstants.MISFIRE_IGNORE_MISFIRES ->
          cb.withMisfireHandlingInstructionIgnoreMisfires();
      case ScheduleConstants.MISFIRE_FIRE_AND_PROCEED ->
          cb.withMisfireHandlingInstructionFireAndProceed();
      case ScheduleConstants.MISFIRE_DO_NOTHING -> cb.withMisfireHandlingInstructionDoNothing();
      default ->
          throw new TaskException(
              "Invalid misfire policy: " + job.getMisfirePolicy(), TaskException.Code.CONFIG_ERROR);
    };
  }

  /** Checks if an invoke target is within the configured whitelist. */
  public static boolean whiteList(String invokeTarget) {
    String packageName = StringUtils.substringBefore(invokeTarget, "(");
    int dotCount = StringUtils.countMatches(packageName, ".");
    if (dotCount > 1) {
      return StringUtils.startsWithAny(invokeTarget, Constants.JOB_WHITELIST);
    }

    Object bean = SpringUtils.getBean(StringUtils.split(invokeTarget, ".")[0]);
    String beanPackage = bean.getClass().getPackage().getName();
    return StringUtils.startsWithAny(beanPackage, Constants.JOB_WHITELIST)
        && !StringUtils.startsWithAny(beanPackage, Constants.JOB_ERROR_LIST);
  }
}
