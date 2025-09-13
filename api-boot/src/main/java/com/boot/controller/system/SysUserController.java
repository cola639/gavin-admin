package com.boot.controller.system;

import com.api.common.controller.BaseController;
import com.api.common.domain.entity.SysUser;
import com.api.common.utils.pagination.TableDataInfo;

import com.api.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for managing user information.
 */
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController extends BaseController {

    private final SysUserService userService;

    /**
     * Get paginated list of users (with fixed params for now).
     */
//    @GetMapping("/system/user/list")
//    public TableDataInfo list(SysUser user) {
//        // 固定参数（先写死，避免复杂 Map）
//        Map<String, Object> params = new HashMap<>();
//        params.put("beginTime", null); // 可以改成 Date
//        params.put("endTime", null);
//
//        // 固定分页（第 0 页，每页 10 条）
//        PageRequest pageable = PageRequest.of(0, 10);
//
//        Page<SysUser> page = userService.selectUserList(user, params, pageable);
//
//        return getDataTable(page.getContent(), page.getTotalElements());
//    }
}
