package com.api.boot.controller.test;

import com.api.common.domain.AjaxResult;
import com.api.framework.annotation.RateLimiter;
import com.api.framework.annotation.RepeatSubmit;
import com.api.framework.enums.LimitType;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/** Dev-only endpoints to exercise annotation behavior. */
@Profile("dev")
@RestController
@RequestMapping("/test/annotations")
public class AnnotationTestController {

  @GetMapping("/rate-limit")
  @RateLimiter(time = 10, count = 3, limitType = LimitType.IP, message = "Rate limit exceeded.")
  public AjaxResult rateLimit() {
    Map<String, Object> data = new HashMap<>();
    data.put("timestamp", Instant.now().toString());
    data.put("limitType", LimitType.IP.name());
    data.put("windowSeconds", 10);
    data.put("limit", 3);
    return AjaxResult.success("rate limiter ok", data);
  }

  @PostMapping("/repeat")
  @RepeatSubmit(interval = 3000, message = "Duplicate submission detected.")
  public AjaxResult repeat(@RequestBody(required = false) Map<String, Object> payload) {
    Map<String, Object> data = new HashMap<>();
    data.put("timestamp", Instant.now().toString());
    if (payload != null) {
      data.put("payload", payload);
    }
    return AjaxResult.success("repeat submit ok", data);
  }
}
