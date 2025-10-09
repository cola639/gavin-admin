package com.api.quartz.util;

import com.api.quartz.domain.SysJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

/** Quartz job handler that disallows concurrent execution. */
@Slf4j
@Component
@DisallowConcurrentExecution // Prevents concurrent execution of the same job
public class QuartzDisallowConcurrentExecution extends AbstractQuartzJob {

  @Override
  protected void doExecute(JobExecutionContext context, SysJob sysJob) throws Exception {
    JobInvokeUtil.invokeMethod(sysJob);
    log.debug("Executed non-concurrent job: {}", sysJob.getJobName());
  }
}
