package com.api.framework.manger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

// asyncFactory.recordLogininfor(userName, Constants.LOGOUT, "logout success");
@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncFactory {

  //  private final ISysLogininforService logininforService;
  //  private final ISysOperLogService operLogService;
  //
  //  /** Record login information asynchronously. */
  //  @Async("taskExecutor")
  //  public void recordLogininfor(String username, String status, String message, Object... args) {
  //    // your existing logic...
  //    log.info("Login info: user={}, status={}, message={}", username, status, message);
  //    // Save to DB
  //    logininforService.insertLogininfor(new SysLogininfor(/*...*/ ));
  //  }
  //
  //  /** Record operation log asynchronously. */
  //  @Async("taskExecutor")
  //  public void recordOper(SysOperLog operLog) {
  //    // your existing logic...
  //    operLogService.insertOperlog(operLog);
  //  }
}
