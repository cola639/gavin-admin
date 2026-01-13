package com.api.boot.controller.system;

import com.api.common.annotation.Log;
import com.api.common.domain.AjaxResult;
import com.api.common.enums.LogBusinessType;
import com.api.system.domain.system.SysConfig;
import com.api.system.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** REST Controller for managing system configuration. */
@Slf4j
@RestController
@RequestMapping("/api/system/config")
@RequiredArgsConstructor
public class SysConfigController {

  private final SysConfigService configService;

  @GetMapping("/list")
  public AjaxResult list(SysConfig filter) {
    List<SysConfig> list = configService.findAll(filter);
    return AjaxResult.success(list);
  }

  @GetMapping("/{id}")
  public AjaxResult getById(@PathVariable Long id) {
    return AjaxResult.success(configService.findById(id));
  }

  @GetMapping("/key/{key}")
  public AjaxResult getByKey(@PathVariable String key) {
    return AjaxResult.success(configService.findByKey(key));
  }

  @Log(title = "System Config", businessType = LogBusinessType.INSERT)
  @PostMapping
  public AjaxResult add(@Validated @RequestBody SysConfig config) {
    return AjaxResult.success(configService.create(config));
  }

  @Log(title = "System Config", businessType = LogBusinessType.UPDATE)
  @PutMapping
  public AjaxResult edit(@Validated @RequestBody SysConfig config) {
    return AjaxResult.success(configService.update(config));
  }

  @Log(title = "System Config", businessType = LogBusinessType.DELETE)
  @DeleteMapping("/{ids}")
  public AjaxResult remove(@PathVariable Long[] ids) {
    configService.deleteByIds(ids);
    return AjaxResult.success();
  }

  @Log(title = "System Config", businessType = LogBusinessType.CLEAN)
  @DeleteMapping("/reload")
  public AjaxResult reloadCache() {
    configService.reloadCache();
    return AjaxResult.success("Cache reloaded successfully");
  }
}
