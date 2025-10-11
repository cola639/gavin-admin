package com.api.boot.controller.monitor;

import com.api.common.domain.AjaxResult;
import com.api.framework.domain.server.Server;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Server Information Controller */
@RestController
@RequestMapping("/monitor/server")
public class ServerController {

  @GetMapping()
  public AjaxResult getInfo() throws Exception {
    Server server = new Server();
    server.collectSystemInfo();
    return AjaxResult.success(server);
  }
}
