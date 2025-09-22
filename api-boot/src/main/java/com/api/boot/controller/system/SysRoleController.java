package com.api.boot.controller.system;

import com.api.common.controller.BaseController;
import com.api.common.domain.AjaxResult;
import com.api.common.domain.entity.LoginUser;
import com.api.common.domain.entity.SysRole;
import com.api.common.domain.entity.SysUser;
import com.api.common.utils.pagination.TableDataInfo;
import com.api.system.domain.SysUserRole;
import com.api.system.service.*;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Role management controller. Handles CRUD operations for system roles, role permissions, data
 * scopes, and user-role assignments.
 */
@Slf4j
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class SysRoleController extends BaseController {

  private final SysRoleService roleService;
  //  private final TokenService tokenService;
  //  private final SysPermissionService permissionService;
  private final SysUserService userService;
  private final SysDeptService deptService;

  /** Get role list */
  @GetMapping("/list")
  public TableDataInfo list(SysRole role) {
    Map<String, Object> params = new HashMap<>();
    params.put("beginTime", null);
    params.put("endTime", null);

    Pageable pageable = PageRequest.of(0, 10);
    Page<SysRole> page = roleService.selectRoleList(role, params, pageable);

    return getDataTable(page);
  }

  //  /** Export role list to Excel */
  //  @Log(title = "Role Management", businessType = BusinessType.EXPORT)
  //  @PreAuthorize("@ss.hasPermi('system:role:export')")
  //  @PostMapping("/export")
  //  public void export(HttpServletResponse response, SysRole role) {
  //    List<SysRole> list = roleService.selectRoleList(role);
  //    ExcelUtil<SysRole> util = new ExcelUtil<>(SysRole.class);
  //    util.exportExcel(response, list, "Role Data");
  //  }
  //
  /** Get role details by ID */
  @GetMapping("/{roleId}")
  public AjaxResult getInfo(@PathVariable Long roleId) {
    return AjaxResult.success(roleService.selectRoleById(roleId));
  }

  //
  //  /** Create a new role */
  //  @PreAuthorize("@ss.hasPermi('system:role:add')")
  //  @Log(title = "Role Management", businessType = BusinessType.INSERT)
  @PostMapping
  public AjaxResult add(@Validated @RequestBody SysRole role) {

    // role.setCreateBy(getUsername());
    return AjaxResult.success(roleService.createRole(role));
  }

  //
  //  /** Update an existing role */
  //  @PreAuthorize("@ss.hasPermi('system:role:edit')")
  //  @Log(title = "Role Management", businessType = BusinessType.UPDATE)
  @PutMapping
  public AjaxResult edit(@Validated @RequestBody SysRole role) {
    //      roleService.checkRoleAllowed(role);
    //      roleService.checkRoleDataScope(role.getRoleId());

    //      if (!roleService.checkRoleNameUnique(role)) {
    //        return error("Failed to update role '" + role.getRoleName() + "': Role name already
    //   exists");
    //      }
    //      if (!roleService.checkRoleKeyUnique(role)) {
    //        return error("Failed to update role '" + role.getRoleName() + "': Role key already
    //   exists");
    //      }

    //    role.setUpdateBy(getUsername());
    roleService.updateRole(role);

    return AjaxResult.success("Operation successful");
  }

  @DeleteMapping("/{roleIds}")
  public AjaxResult remove(@PathVariable Long[] roleIds) {
    return AjaxResult.success(roleService.deleteRoleByIds(roleIds));
  }

  //
  //  /** Update role data scope */
  //  @PreAuthorize("@ss.hasPermi('system:role:edit')")
  //  @Log(title = "Role Management", businessType = BusinessType.UPDATE)
  //  @PutMapping("/dataScope")
  //  public AjaxResult dataScope(@RequestBody SysRole role) {
  //    roleService.checkRoleAllowed(role);
  //    roleService.checkRoleDataScope(role.getRoleId());
  //    return toAjax(roleService.authDataScope(role));
  //  }
  //
  //  /** Change role status */
  //  @PreAuthorize("@ss.hasPermi('system:role:edit')")
  //  @Log(title = "Role Management", businessType = BusinessType.UPDATE)
  //  @PutMapping("/changeStatus")
  //  public AjaxResult changeStatus(@RequestBody SysRole role) {
  //    roleService.checkRoleAllowed(role);
  //    roleService.checkRoleDataScope(role.getRoleId());
  //    role.setUpdateBy(getUsername());
  //    return toAjax(roleService.updateRoleStatus(role));
  //  }
  //
  //  /** Delete roles */
  //  @PreAuthorize("@ss.hasPermi('system:role:remove')")
  //  @Log(title = "Role Management", businessType = BusinessType.DELETE)

  //
  //  /** Get all roles for dropdown selection */
  //  @PreAuthorize("@ss.hasPermi('system:role:query')")
  //  @GetMapping("/optionselect")
  //  public AjaxResult optionselect() {
  //    return success(roleService.selectRoleAll());
  //  }
  //
  //  /** List users already assigned to roles */
  //  @PreAuthorize("@ss.hasPermi('system:role:list')")
  //  @GetMapping("/authUser/allocatedList")
  //  public TableDataInfo allocatedList(SysUser user) {
  //    startPage();
  //    List<SysUser> list = userService.selectAllocatedList(user);
  //    return getDataTable(list);
  //  }
  //
  //  /** List users not yet assigned to roles */
  //  @PreAuthorize("@ss.hasPermi('system:role:list')")
  //  @GetMapping("/authUser/unallocatedList")
  //  public TableDataInfo unallocatedList(SysUser user) {
  //    startPage();
  //    List<SysUser> list = userService.selectUnallocatedList(user);
  //    return getDataTable(list);
  //  }
  //
  //  /** Revoke user-role assignment */
  //  @PreAuthorize("@ss.hasPermi('system:role:edit')")
  //  @Log(title = "Role Management", businessType = BusinessType.GRANT)
  //  @PutMapping("/authUser/cancel")
  //  public AjaxResult cancelAuthUser(@RequestBody SysUserRole userRole) {
  //    return toAjax(roleService.deleteAuthUser(userRole));
  //  }
  //
  //  /** Batch revoke user-role assignments */
  //  @PreAuthorize("@ss.hasPermi('system:role:edit')")
  //  @Log(title = "Role Management", businessType = BusinessType.GRANT)
  //  @PutMapping("/authUser/cancelAll")
  //  public AjaxResult cancelAuthUserAll(Long roleId, Long[] userIds) {
  //    return toAjax(roleService.deleteAuthUsers(roleId, userIds));
  //  }
  //
  //  /** Batch assign users to role */
  //  @PreAuthorize("@ss.hasPermi('system:role:edit')")
  //  @Log(title = "Role Management", businessType = BusinessType.GRANT)
  //  @PutMapping("/authUser/selectAll")
  //  public AjaxResult selectAuthUserAll(Long roleId, Long[] userIds) {
  //    roleService.checkRoleDataScope(roleId);
  //    return toAjax(roleService.insertAuthUsers(roleId, userIds));
  //  }
  //
  //  /** Get department tree for a specific role */
  //  @PreAuthorize("@ss.hasPermi('system:role:query')")
  //  @GetMapping("/deptTree/{roleId}")
  //  public AjaxResult deptTree(@PathVariable("roleId") Long roleId) {
  //    AjaxResult ajax = AjaxResult.success();
  //    ajax.put("checkedKeys", deptService.selectDeptListByRoleId(roleId));
  //    ajax.put("depts", deptService.selectDeptTreeList(new SysDept()));
  //    return ajax;
  //  }
  //
  //  /** Refresh login user's permissions in cache */
  //  private void refreshLoginUserPermissions() {
  //    LoginUser loginUser = getLoginUser();
  //    if (StringUtils.isNotNull(loginUser.getUser()) && !loginUser.getUser().isAdmin()) {
  //      loginUser.setUser(userService.selectUserByUserName(loginUser.getUser().getUserName()));
  //      loginUser.setPermissions(permissionService.getMenuPermission(loginUser.getUser()));
  //      tokenService.setLoginUser(loginUser);
  //    }
  //  }
}
