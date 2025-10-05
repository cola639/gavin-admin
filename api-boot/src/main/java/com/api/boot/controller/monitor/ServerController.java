package com.api.boot.controller.monitor;

import com.api.common.domain.AjaxResult;
import com.api.framework.domain.Server;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务器监控
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/monitor/server")
public class ServerController {

  @PreAuthorize("@ss.hasPermi('monitor:server:list')")
  @GetMapping()
  public AjaxResult getInfo() throws Exception {
    Server server = new Server();
    server.collectSystemInfo();
    return AjaxResult.success(server);
  }
}
