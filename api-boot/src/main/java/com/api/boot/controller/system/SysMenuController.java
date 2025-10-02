package com.api.boot.controller.system;

import com.api.common.controller.BaseController;
import com.api.common.domain.AjaxResult;
import com.api.persistence.domain.SysMenu;
import com.api.system.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
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

  /** Add a new menu item. */
  @PostMapping
  public AjaxResult add(@Validated @RequestBody SysMenu menu) {
    //    if (!menuService.checkMenuNameUnique(menu)) {
    //      return error("新增菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
    //    } else if (UserConstants.YES_FRAME.equals(menu.getIsFrame())
    //        && !StringUtils.ishttp(menu.getPath())) {
    //      return error("新增菜单'" + menu.getMenuName() + "'失败，地址必须以http(s)://开头");
    //    }
    //    menu.setCreateBy(getUsername());
    return AjaxResult.success(sysMenuService.insertMenu(menu));
  }

  /** Add a new menu item. */
  @PostMapping
  public AjaxResult update(@Validated @RequestBody SysMenu menu) {
    //    if (!menuService.checkMenuNameUnique(menu)) {
    //      return error("新增菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
    //    } else if (UserConstants.YES_FRAME.equals(menu.getIsFrame())
    //        && !StringUtils.ishttp(menu.getPath())) {
    //      return error("新增菜单'" + menu.getMenuName() + "'失败，地址必须以http(s)://开头");
    //    }
    //    menu.setCreateBy(getUsername());
    return AjaxResult.success(sysMenuService.insertMenu(menu));
  }

  @DeleteMapping("/{menuId}")
  public AjaxResult remove(@PathVariable("menuId") Long menuId) {
    //    if (menuService.hasChildByMenuId(menuId))
    //    {
    //      return warn("存在子菜单,不允许删除");
    //    }
    //    if (menuService.checkMenuExistRole(menuId))
    //    {
    //      return warn("菜单已分配,不允许删除");
    //    }
    sysMenuService.deleteMenuById(menuId);
    return AjaxResult.success();
  }
}
