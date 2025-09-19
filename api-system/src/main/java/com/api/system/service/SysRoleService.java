package com.api.system.service;

import com.api.common.domain.entity.SysRole;
import com.api.common.domain.entity.SysUser;
import com.api.common.utils.jpa.SpecificationBuilder;
import com.api.system.repository.SysRoleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SysRoleService {

  private final SysRoleRepository roleRepository;

  /** List roles with optional filters (using Specification for dynamic queries) */

  /** Insert role */
  @Transactional
  public SysRole insertRole(SysRole role) {

    return roleRepository.save(role);
  }

  /** Update role */
  @Transactional
  public SysRole updateRole(SysRole role) {

    return roleRepository.save(role);
  }

  /** Soft delete role */
  @Transactional
  public int deleteRoleById(Long roleId) {
    return roleRepository.softDeleteById(roleId);
  }

  /** Soft delete multiple roles */
  @Transactional
  public int deleteRoleByIds(List<Long> roleIds) {
    return roleRepository.softDeleteByIds(roleIds);
  }

  /**
   * Query role list with dynamic conditions.
   *
   * @param role filter role entity
   * @param params additional parameters like beginTime, endTime, dataScope
   * @param pageable pagination info
   * @return paginated list of SysRole
   */
  public Page<SysRole> selectRoleList(SysRole role, Map<String, Object> params, Pageable pageable) {
    Specification<SysRole> spec =
        SpecificationBuilder.<SysRole>builder()
            // Equivalent to r.del_flag = '0'
            .eq("delFlag", "0")
            // r.role_id = ?
            .eq("roleId", role.getRoleId())
            // r.role_name LIKE ?
            .like("roleName", role.getRoleName())
            // r.status = ?
            .eq("status", role.getStatus())
            // r.role_key LIKE ?
            .like("roleKey", role.getRoleKey())
            // create_time between beginTime and endTime
            .between(
                "createTime",
                (LocalDateTime) params.get("beginTime"),
                (LocalDateTime) params.get("endTime"))
            .build();

    return roleRepository.findAll(spec, pageable);
  }

  public SysRole selectRoleById(Long roleId) {
    return roleRepository.findById(roleId).orElse(null);
  }
}
