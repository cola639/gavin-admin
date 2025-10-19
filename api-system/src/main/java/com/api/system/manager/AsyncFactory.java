package com.api.system.manager;

import com.api.persistence.domain.system.SysOperLog;
import com.api.system.service.SysOperLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncFactory {

  // private final ISysLogininforService logininforService;

  private final SysOperLogService operLogService;

  /** Record login information asynchronously. */
  //  @Async("taskExecutor")
  //  public void recordLogininfor(String username, String status, String message, Object... args) {
  //    // your existing logic...
  //    log.info("Login info: user={}, status={}, message={}", username, status, message);
  //    // Save to DB
  //    logininforService.insertLogininfor(new SysLogininfor(/*...*/ ));
  //  }

  /** Record operation log asynchronously. */
  @Async("taskExecutor")
  public void recordOper(SysOperLog operLog) {
    operLogService.insertOperLog(operLog);
  }
}
