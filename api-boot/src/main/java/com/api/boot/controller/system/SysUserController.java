package com.api.boot.controller.system;

import com.api.common.controller.BaseController;
import com.api.common.domain.entity.SysUser;
import com.api.common.utils.pagination.TableDataInfo;

import com.api.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    
    @GetMapping("/list")
    public TableDataInfo list(SysUser user) {
        Map<String, Object> params = new HashMap<>();
        params.put("beginTime", null);
        params.put("endTime", null);

        Pageable pageable = PageRequest.of(0, 10);
        Page<SysUser> page = userService.selectUserList(user, params, pageable);

        return getDataTable(page);
    }
}
