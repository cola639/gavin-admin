package com.api.boot.controller.system;

import com.api.common.controller.BaseController;
import com.api.common.domain.AjaxResult;
import com.api.common.domain.SysMenu;
import com.api.common.domain.SysMenuDTOs;
import com.api.persistence.repository.system.SysRoleMenuRepository;
import com.api.system.service.SysMenuService;
import jakarta.validation.Valid;
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
  private final SysRoleMenuRepository sysRoleMenuRepository;

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
    menu.setCreateBy(getUsername());
    return AjaxResult.success(sysMenuService.insertMenu(menu));
  }

  /** Add a new menu item. */
  @PutMapping
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

  @PutMapping("/update-orders")
  public AjaxResult updateOrders(@RequestBody @Valid List<SysMenuDTOs.OrderUpdateRequest> orders) {
    if (orders == null || orders.isEmpty()) {
      return AjaxResult.error("Request body can not be empty.");
    }

    int updated = sysMenuService.updateMenuOrders(orders, getUsername());

    AjaxResult ajax = AjaxResult.success("Menu order updated successfully.");
    ajax.put("updated", updated);
    return ajax;
  }

  @DeleteMapping("/{menuId}")
  public AjaxResult remove(@PathVariable("menuId") Long menuId) {
    if (sysMenuService.hasChildByMenuId(menuId)) {
      return warn("Cannot delete menu: it has child menus.");
    }

    if (sysMenuService.isMenuAssignedToAnyRole(menuId)) {
      return warn("Cannot delete menu: it is assigned to one or more roles.");
    }

    sysMenuService.deleteMenuById(menuId);
    return AjaxResult.success("Menu deleted successfully.");
  }
}
