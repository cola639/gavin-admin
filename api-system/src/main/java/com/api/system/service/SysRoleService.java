package com.api.system.service;

import com.api.common.domain.entity.SysRole;
import com.api.common.domain.entity.SysUser;
import com.api.common.utils.SecurityUtils;
import com.api.common.utils.StringUtils;
import com.api.common.utils.springUtils.SpringUtils;
import com.api.framework.annotation.DataScope;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class SysRoleService {

    public void checkRoleDataScope(Long... roleIds) {
//        if (!SysUser.isAdmin(SecurityUtils.getUserId())) {
//            for (Long roleId : roleIds) {
//                SysRole role = new SysRole();
//                role.setRoleId(roleId);
//                List<SysRole> roles = SpringUtils.getAopProxy(this).selectRoleList(role);
//                if (StringUtils.isEmpty(roles)) {
//                    throw new ServiceException("没有权限访问角色数据！");
//                }
//            }
//        }
    }

//    @DataScope(deptAlias = "d")
//    public List<SysRole> selectRoleList(SysRole role) {
//
//    }
}

