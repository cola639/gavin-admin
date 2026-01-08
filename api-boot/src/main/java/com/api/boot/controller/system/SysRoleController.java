package com.api.boot.controller.system;

import com.api.common.controller.BaseController;
import com.api.common.domain.AjaxResult;
import com.api.common.domain.SysRole;
import com.api.common.domain.SysUserDTO;
import com.api.common.utils.pagination.TableDataInfo;
import com.api.system.service.SysRoleService;
import com.api.system.service.SysUserService;
import com.api.system.service.SysDeptService;
import com.api.system.service.SysMenuService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Validated
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class SysRoleController extends BaseController {

  private final SysRoleService roleService;
  private final SysUserService userService;
  private final SysDeptService deptService;
  private final SysMenuService sysMenuService;

  @GetMapping("/list")
  public TableDataInfo list(
      @RequestParam(defaultValue = "1") int pageNum,
      @RequestParam(defaultValue = "10") int pageSize,
      SysRole role) {

    Map<String, Object> params = new HashMap<>();
    params.put("beginTime", null);
    params.put("endTime", null);

    Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
    Page<SysRole> page = roleService.selectRoleList(role, params, pageable);

    return getDataTable(page);
  }

  @GetMapping("/{roleId}")
  public AjaxResult getInfo(@PathVariable Long roleId) {
    return AjaxResult.success(roleService.selectRoleById(roleId));
  }

  @PostMapping
  public AjaxResult add(@RequestBody SysRole role) {
    role.setCreateBy(getUsername());
    return AjaxResult.success(roleService.createRole(role));
  }

  @PutMapping
  public AjaxResult edit(@RequestBody SysRole role) {
    role.setUpdateBy(getUsername());
    roleService.updateRole(role);
    return AjaxResult.success("Operation successful");
  }

  @GetMapping("/roleMenuTreeselect/{roleId}")
  public AjaxResult roleMenuTreeselect(@PathVariable("roleId") Long roleId) {

    AjaxResult ajax = AjaxResult.success();
    ajax.put("checkedKeys", sysMenuService.selectMenuByRoleId(roleId));
    ajax.put("menus", sysMenuService.selectMenuTreeByRoleId(roleId));
    return ajax;
  }

  @DeleteMapping("/{roleIds}")
  public AjaxResult remove(@PathVariable Long[] roleIds) {
    return AjaxResult.success(roleService.deleteRoleByIds(roleIds));
  }

  @GetMapping("/optionselect")
  public AjaxResult optionselect() {
    return AjaxResult.success(roleService.selectRoleAll());
  }

  @GetMapping("/authUser/allocatedList")
  public TableDataInfo<SysUserDTO> allocatedList(
      @RequestParam("roleId") Long roleId,
      SysUserDTO filter,
      @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
      @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize) {

    Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
    Page<SysUserDTO> page = roleService.getAllocatedUsersByRoleId(roleId, filter, pageable);
    return TableDataInfo.success(page);
  }

  @GetMapping("/authUser/unallocatedList")
  public TableDataInfo<SysUserDTO> unallocatedList(
      @RequestParam("roleId") @NotNull(message = "roleId can not be null") Long roleId,
      SysUserDTO filter,
      @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
      @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize) {

    Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
    Page<SysUserDTO> page = roleService.getUnAllocatedUsersByRoleId(roleId, filter, pageable);
    return TableDataInfo.success(page);
  }

  /** Batch revoke users from role (like ruoyi cancelAll). */
  @PutMapping("/authUser/batch-revoke")
  public AjaxResult batchRevoke(
      @RequestParam("roleId") @NotNull Long roleId,
      @RequestParam("userIds") @NotNull Long[] userIds) {

    if (userIds.length == 0) {
      return AjaxResult.error("userIds can not be empty");
    }

    Long affected = roleService.revokeUsersFromRole(roleId, Arrays.asList(userIds));
    AjaxResult ajax = AjaxResult.success("Batch revoke success");
    ajax.put("affected", affected);
    return ajax;
  }

  /** Batch assign users to role (like ruoyi selectAll). */
  @PutMapping("/authUser/batch-assign")
  public AjaxResult batchAssign(
      @RequestParam("roleId") @NotNull Long roleId,
      @RequestParam("userIds") @NotNull Long[] userIds) {

    if (userIds.length == 0) {
      return AjaxResult.error("userIds can not be empty");
    }

    int affected = roleService.assignUsersToRole(roleId, Arrays.asList(userIds));
    AjaxResult ajax = AjaxResult.success("Batch assign success");
    ajax.put("affected", affected);
    return ajax;
  }
}
