package com.api.quartz.util;

import com.api.quartz.domain.SysJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

/** Quartz job handler that allows concurrent execution. */
@Slf4j
@Component
public class QuartzJobExecution extends AbstractQuartzJob {

  @Override
  protected void doExecute(JobExecutionContext context, SysJob sysJob) throws Exception {
    JobInvokeUtil.invokeMethod(sysJob);
    log.debug("Executed concurrent job: {}", sysJob.getJobName());
  }
}
