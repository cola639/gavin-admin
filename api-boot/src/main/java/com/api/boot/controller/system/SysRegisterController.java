package com.api.boot.controller.system;

import com.api.common.controller.BaseController;
import com.api.common.domain.AjaxResult;
import com.api.common.utils.StringUtils;
import com.api.framework.service.SysRegisterService;
import com.api.persistence.domain.common.RegisterBody;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 注册验证
 *
 * @author ruoyi
 */
@RequiredArgsConstructor
@RestController
public class SysRegisterController extends BaseController {
  private final SysRegisterService registerService;

  @PostMapping("/register")
  public AjaxResult register(@RequestBody RegisterBody user) {
    String msg = registerService.register(user);
    return StringUtils.isEmpty(msg) ? success() : error(msg);
  }
}
