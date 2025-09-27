package com.api.boot.controller.system;

import com.api.common.controller.BaseController;
import com.api.common.domain.AjaxResult;
import com.api.system.domain.SysMenu;
import com.api.system.service.SysMenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SysMenuController - Menu management endpoints (JPA-based).
 *
 * @author
 */
@Slf4j
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class SysMenuController extends BaseController {

  private final SysMenuService sysMenuService;

  /** Get menu list with optional filters. */
  @GetMapping("/list")
  public AjaxResult list(SysMenu menu) {
    List<SysMenu> menus = sysMenuService.getMenuList(menu);
    return AjaxResult.success(menus);
  }
}
