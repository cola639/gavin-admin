package com.api.boot.controller.system;

import com.api.common.controller.BaseController;
import com.api.common.domain.AjaxResult;
import com.api.common.domain.SysRole;
import com.api.common.domain.SysUser;
import com.api.common.domain.SysUserDTO;
import com.api.common.utils.SecurityUtils;
import com.api.common.utils.pagination.TableDataInfo;

import com.api.framework.annotation.RepeatSubmit;
import com.api.system.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** REST controller for managing user information. */
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController extends BaseController {

  private final SysUserService userService;

  private final SysDeptService sysDeptService;

  private final SysRoleService sysRoleService;

  private final SysPostService sysPostService;

  /** Get paginated list of users (with fixed params for now). */
  @PostMapping("/list")
  public TableDataInfo<SysUserDTO> list(
      @RequestBody SysUser user,
      @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
      @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize) {

    Map<String, Object> params = new HashMap<>();
    params.put("beginTime", null);
    params.put("endTime", null);

    // Spring pageable uses 0-based page index
    Pageable pageable = PageRequest.of(Math.max(pageNum - 1, 0), pageSize);

    Page<SysUserDTO> page = userService.selectUserList(user, params, pageable);

    return TableDataInfo.success(page); // ✅ Auto wraps into front-end compatible format
  }

  //    public void export(HttpServletResponse response, SysUser user) {
  //        List<SysUser> list = userService.selectUserList(user);
  //        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
  //        util.exportExcel(response, list, "用户数据");
  //    }
  //
  //    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception {
  //        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
  //        List<SysUser> userList = util.importExcel(file.getInputStream());
  //        String operName = getUsername();
  //        String message = userService.importUser(userList, updateSupport, operName);
  //        return success(message);
  //    }
  //

  @GetMapping(value = {"/", "/{userId}"})
  public AjaxResult getInfo(@PathVariable(value = "userId", required = false) Long userId) {
    AjaxResult ajax = AjaxResult.success();
    //    if (StringUtils.isNotNull(userId)) {
    //      userService.checkUserDataScope(userId);
    //      SysUser sysUser = userService.selectUserById(userId);
    //      ajax.put(AjaxResult.DATA_TAG, sysUser);
    //      ajax.put("postIds", postService.selectPostListByUserId(userId));
    //      ajax.put(
    //          "roleIds",
    //          sysUser.getRoles().stream().map(SysRole::getRoleId).collect(Collectors.toList()));
    //    }
    List<SysRole> roles = sysRoleService.selectRoleAll();
    ajax.put("roles", roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
    ajax.put("posts", sysPostService.getAllPosts());
    return ajax;
  }

  @PostMapping
  @RepeatSubmit(message = "Please don’t submit twice quickly.")
  public AjaxResult add(@Validated @RequestBody SysUser user) {
    user.setCreateBy(getUsername());
    user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));

    userService.createUser(user);

    return AjaxResult.success("Created user successfully! ");
  }

  @PutMapping
  public AjaxResult update(@Validated @RequestBody SysUser user) {

    SysUser updatedUser = userService.updateUser(user);
    return AjaxResult.success(updatedUser);
  }

  @DeleteMapping("/{userIds}")
  public AjaxResult remove(@PathVariable Long[] userIds) {
    //        if (ArrayUtils.contains(userIds, getUserId())) {
    //            return AjaxResult.error("Not allow delete current user");
    //        }
    userService.deleteUserByIds(userIds);
    return AjaxResult.success();
  }

  //    @PutMapping("/resetPwd")
  //    public AjaxResult resetPwd(@RequestBody SysUser user) {
  //        userService.checkUserAllowed(user);
  //        userService.checkUserDataScope(user.getUserId());
  //        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
  //        user.setUpdateBy(getUsername());
  //        return toAjax(userService.resetPwd(user));
  //    }
  //
  //    @PutMapping("/changeStatus")
  //    public AjaxResult changeStatus(@RequestBody SysUser user) {
  //        userService.checkUserAllowed(user);
  //        userService.checkUserDataScope(user.getUserId());
  //        user.setUpdateBy(getUsername());
  //        return toAjax(userService.updateUserStatus(user));
  //    }
  //
  //    @GetMapping("/authRole/{userId}")
  //    public AjaxResult authRole(@PathVariable("userId") Long userId) {
  //        AjaxResult ajax = AjaxResult.success();
  //        SysUser user = userService.selectUserById(userId);
  //        List<SysRole> roles = roleService.selectRolesByUserId(userId);
  //        ajax.put("user", user);
  //        ajax.put("roles", SysUser.isAdmin(userId) ? roles : roles.stream().filter(r ->
  // !r.isAdmin()).collect(Collectors.toList()));
  //        return ajax;
  //    }
  //
  //    @PutMapping("/authRole")
  //    public AjaxResult insertAuthRole(Long userId, Long[] roleIds) {
  //        userService.checkUserDataScope(userId);
  //        roleService.checkRoleDataScope(roleIds);
  //        userService.insertUserAuth(userId, roleIds);
  //        return success();
  //    }
  //
  //    @GetMapping("/deptTree")
  //    public AjaxResult deptTree(SysDept dept) {
  //        return success(deptService.selectDeptTreeList(dept));
  //    }
}
