package com.api.boot.controller.system;

import com.api.common.controller.BaseController;
import com.api.common.domain.AjaxResult;
import com.api.common.domain.SysRole;
import com.api.common.domain.SysUser;
import com.api.common.domain.SysUserDTO;
import com.api.common.utils.SecurityUtils;
import com.api.common.enums.DelFlagEnum;
import com.api.common.utils.StringUtils;
import com.api.common.utils.excel.DictProvider;
import com.api.common.utils.excel.SimpleExcelWriter;
import com.api.common.utils.pagination.TableDataInfo;

import com.api.common.utils.uuid.IdUtils;
import com.api.framework.annotation.RepeatSubmit;
import com.api.framework.exception.ServiceException;
import com.api.system.imports.user.UserImportResult;
import com.api.system.imports.user.UserImportService;
import com.api.system.service.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
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

  private final UserImportService userImportService;

  @Value("${app.default.password:admin123}")
  private String defaultPassword;

  /** Get paginated list of users (with fixed params for now). */
  @PostMapping("/list")
  public TableDataInfo<SysUserDTO> list(
      @RequestBody(required = false) SysUserDTO user,
      @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
      @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize,
      @RequestParam(value = "unpaged", required = false, defaultValue = "false") boolean unpaged) {

    Map<String, Object> params = new HashMap<>();
    params.put("beginTime", null);
    params.put("endTime", null);

    Pageable pageable = unpaged ? Pageable.unpaged() : PageRequest.of(pageNum - 1, pageSize);

    // if request body is null, use empty criteria
    SysUserDTO criteria = (user != null) ? user : new SysUserDTO();

    Page<SysUserDTO> page = userService.selectUserList(criteria, params, pageable);

    return TableDataInfo.success(page);
  }

  @PostMapping("/export")
  public void export(HttpServletResponse response, @RequestBody(required = false) SysUserDTO user) {

    Map<String, Object> params = new HashMap<>();
    params.put("beginTime", null);
    params.put("endTime", null);

    SysUserDTO criteria = (user != null) ? user : new SysUserDTO();

    // ✅ unpaged: let service/repo fetch all rows matching spec
    Page<SysUserDTO> page = userService.selectUserList(criteria, params, Pageable.unpaged());

    DictProvider dict = (dictType, value) -> null;

    SimpleExcelWriter.export(
        page.getContent(), // List<SysUserDTO>
        SysUserDTO.class,
        response,
        "UserList", // file name (without .xlsx)
        "User Lsit", // sheet name
        dict);
  }

  //
  //    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception {
  //        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
  //        List<SysUser> userList = util.importExcel(file.getInputStream());
  //        String operName = getUsername();
  //        String message = userService.importUser(userList, updateSupport, operName);
  //        return success(message);
  //    }
  //

  @PostMapping("/import")
  public AjaxResult importUsers(
      @RequestPart MultipartFile file,
      @RequestParam(value = "updateSupport", defaultValue = "false") boolean updateSupport) {
    validateImportFile(file);
    try (java.io.InputStream inputStream = file.getInputStream()) {
      UserImportResult result = userImportService.importUsers(inputStream, updateSupport, false);
      return AjaxResult.success("Import completed", result);
    } catch (IOException e) {
      throw new ServiceException("Failed to read uploaded file: " + e.getMessage());
    }
  }

  @PostMapping("/import/preview")
  public AjaxResult previewImport(
      @RequestPart MultipartFile file,
      @RequestParam(value = "updateSupport", defaultValue = "false") boolean updateSupport) {
    validateImportFile(file);
    try (java.io.InputStream inputStream = file.getInputStream()) {
      UserImportResult result = userImportService.importUsers(inputStream, updateSupport, true);
      return AjaxResult.success("Import preview completed", result);
    } catch (IOException e) {
      throw new ServiceException("Failed to read uploaded file: " + e.getMessage());
    }
  }

  @GetMapping("/info")
  public AjaxResult getInfo(@RequestParam(value = "userId", required = false) Long userId) {
    AjaxResult ajax = AjaxResult.success();
    if (userId != null) {
      SysUser sysUser =
          Optional.ofNullable(userService.selectUserById(userId))
              .orElseThrow(() -> new ServiceException("User not found for id=" + userId));

      ajax.put(AjaxResult.DATA_TAG, sysUser);
      ajax.put("postIds", sysPostService.selectPostListByUserId(userId));
      ajax.put(
          "roleIds",
          sysUser.getRoles().stream().map(SysRole::getRoleId).collect(Collectors.toList()));
    }

    List<SysRole> roles = sysRoleService.selectRoleAll();
    ajax.put("roles", roles);
    ajax.put("posts", sysPostService.getAllPosts());
    return ajax;
  }

  @PostMapping
  @RepeatSubmit(message = "Please don’t submit twice quickly.")
  public AjaxResult add(@Validated @RequestBody SysUser user) {
    user.setPassword(defaultPassword);
    user.setCreateBy(getUsername());
    user.setDelFlag(DelFlagEnum.NORMAL.getCode());
    userService.createUser(user);

    return AjaxResult.success("Created user successfully! ");
  }

  @PutMapping
  public AjaxResult update(@Validated @RequestBody SysUserDTO user) {

    SysUser updatedUser = userService.updateUser(user);

    if (updatedUser != null) {
      return AjaxResult.success("Updated user successfully !");
    } else {
      return AjaxResult.success("Updated user failed ！");
    }
  }

  @DeleteMapping("/{userIds}")
  public AjaxResult remove(@PathVariable Long[] userIds) {
    if (ArrayUtils.contains(userIds, getUserId())) {
      return AjaxResult.error("Not allow delete current user");
    }
    userService.deleteUserByIds(userIds);
    return AjaxResult.success("Deleted Successful");
  }

  private void validateImportFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new ServiceException("Excel file is required");
    }
    String filename = file.getOriginalFilename();
    if (filename == null || !filename.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
      throw new ServiceException("Only .xlsx files are supported");
    }
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
