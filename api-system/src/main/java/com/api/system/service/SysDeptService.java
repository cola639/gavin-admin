package com.api.system.service;

import com.api.common.domain.entity.SysDept;
import com.api.common.domain.entity.SysUser;
import com.api.common.utils.SecurityUtils;
import com.api.common.utils.StringUtils;
import com.api.common.utils.springUtils.SpringUtils;
import com.api.framework.annotation.DataScope;
import org.hibernate.service.spi.ServiceException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysDeptService {

    /**
     * 校验部门是否有数据权限
     *
     * @param deptId 部门id
     */
//    public void checkDeptDataScope(Long deptId) {
//        if (!SysUser.isAdmin(SecurityUtils.getUserId()) && StringUtils.isNotNull(deptId)) {
//            SysDept dept = new SysDept();
//            dept.setDeptId(deptId);
//            List<SysDept> depts = SpringUtils.getAopProxy(this).selectDeptList(dept);
//            if (StringUtils.isEmpty(depts)) {
//                throw new ServiceException("没有权限访问部门数据！");
//            }
//        }
//    }

//    @DataScope(deptAlias = "d")
//    public List<SysDept> selectDeptList(SysDept dept) {
//    }

}
