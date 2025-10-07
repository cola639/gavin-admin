package util;

import com.api.common.constant.Constants;
import com.api.common.utils.ExceptionUtil;
import com.api.common.utils.StringUtils;
import com.api.common.utils.bean.BeanUtils;
import com.api.common.utils.springUtils.SpringUtils;
import constant.ScheduleConstants;
import domain.SysJob;
import domain.SysJobLog;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Base class for all Quartz job executions.
 *
 * <p>Provides lifecycle hooks for job execution — before, after, and exception handling.
 */
@Slf4j
@Component
public abstract class AbstractQuartzJob implements Job {

  private static final ThreadLocal<Date> START_TIME = new ThreadLocal<>();

  @Override
  public void execute(JobExecutionContext context) {
    SysJob sysJob = new SysJob();
    BeanUtils.copyBeanProperties(
        sysJob, context.getMergedJobDataMap().get(ScheduleConstants.TASK_PROPERTIES));

    try {
      before(context, sysJob);
      doExecute(context, sysJob);
      after(context, sysJob, null);
    } catch (Exception e) {
      log.error("Job execution failed: {}", sysJob.getJobName(), e);
      after(context, sysJob, e);
    }
  }

  /** Hook executed before job runs. */
  protected void before(JobExecutionContext context, SysJob sysJob) {
    START_TIME.set(new Date());
  }

  /** Hook executed after job completes or fails. */
  protected void after(JobExecutionContext context, SysJob sysJob, Exception e) {
    Date startTime = START_TIME.get();
    START_TIME.remove();

    SysJobLog jobLog = new SysJobLog();
    jobLog.setJobName(sysJob.getJobName());
    jobLog.setJobGroup(sysJob.getJobGroup());
    jobLog.setInvokeTarget(sysJob.getInvokeTarget());
    jobLog.setStartTime(startTime);
    jobLog.setStopTime(new Date());
    jobLog.setJobMessage(
        String.format(
            "%s executed in %d ms",
            sysJob.getJobName(), jobLog.getStopTime().getTime() - startTime.getTime()));

    if (e != null) {
      jobLog.setStatus(Constants.FAIL);
      jobLog.setExceptionInfo(StringUtils.substring(ExceptionUtil.getExceptionMessage(e), 0, 2000));
    } else {
      jobLog.setStatus(Constants.SUCCESS);
    }

    SpringUtils.getBean(ISysJobLogService.class).addJobLog(jobLog);
    log.info("Job '{}' completed with status: {}", sysJob.getJobName(), jobLog.getStatus());
  }

  /** Method implemented by subclasses to execute the actual job logic. */
  protected abstract void doExecute(JobExecutionContext context, SysJob sysJob) throws Exception;
}
