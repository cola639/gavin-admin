package com.api.system.service;

import com.api.common.domain.SysRole;
import com.api.common.domain.SysUser;
import com.api.common.domain.SysUserDTO;
import com.api.common.enums.DelFlagEnum;
import com.api.common.enums.StatusEnum;
import com.api.common.utils.jpa.SpecificationBuilder;

import com.api.persistence.domain.system.SysRoleMenu;
import com.api.persistence.repository.system.SysRoleMenuRepository;
import com.api.persistence.repository.system.SysRoleRepository;
import com.api.persistence.repository.system.SysUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysRoleService {

  private final SysRoleRepository roleRepository;

  private final SysRoleMenuRepository sysRoleMenuRepository;

  private final SysUserRepository sysUserRepository;

  /**
   * Get users allocated to a role (paged).
   *
   * <p>No data-scope for now, so applyScope is always false.
   */
  public Page<SysUserDTO> getAllocatedUsersByRoleId(
      Long roleId, SysUserDTO filter, Pageable pageable) {
    if (roleId == null) {
      throw new ServiceException("roleId can not be null.");
    }

    SysUserDTO criteria = (filter != null) ? filter : new SysUserDTO();

    // NOTE:
    // Even when applyScope=false, we still pass a non-empty deptIds to avoid some JPA providers
    // complaining about "IN ()" binding.
    Boolean applyScope = false;
    List<Long> dummyDeptIds = Collections.singletonList(-1L);

    Page<SysUser> page =
        sysUserRepository.findAllocatedUsers(
            roleId,
            DelFlagEnum.NORMAL.getCode(),
            criteria.getUserName(),
            criteria.getPhonenumber(),
            applyScope, // applyScope
            dummyDeptIds, // deptIds (unused when applyScope=false)
            pageable);

    List<SysUserDTO> dtoList = SysUserDTO.fromEntities(page.getContent());
    return new PageImpl<>(dtoList, pageable, page.getTotalElements());
  }

  /**
   * Get users NOT allocated to a role (paged).
   *
   * <p>No data-scope for now, so applyScope is always false.
   */
  public Page<SysUserDTO> getUnAllocatedUsersByRoleId(
      Long roleId, SysUserDTO filter, Pageable pageable) {
    if (roleId == null) {
      throw new ServiceException("roleId can not be null.");
    }

    SysUserDTO criteria = (filter != null) ? filter : new SysUserDTO();

    boolean applyScope = false;

    // Even when applyScope=false, keep a non-empty list to avoid "IN ()" parameter issues
    List<Long> dummyDeptIds = Collections.singletonList(-1L);

    Page<SysUser> page =
        sysUserRepository.findUnallocatedUsers(
            roleId,
            DelFlagEnum.NORMAL.getCode(),
            criteria.getUserName(),
            criteria.getPhonenumber(),
            applyScope,
            dummyDeptIds,
            pageable);

    List<SysUserDTO> dtoList = SysUserDTO.fromEntities(page.getContent());
    return new PageImpl<>(dtoList, pageable, page.getTotalElements());
  }

  /** Update role */
  @Transactional
  public SysRole updateRole(SysRole role) {
    if (role.getRoleId() == null) throw new ServiceException("Role ID cannot be null");
    if (!roleRepository.existsById(role.getRoleId()))
      throw new ServiceException("Role does not exist");

    // 1. Uniqueness checks
    if (roleRepository.existsByRoleName(role.getRoleName())) {
      throw new ServiceException("Role name '" + role.getRoleName() + "' already exists");
    }
    if (roleRepository.existsByRoleKey(role.getRoleKey())) {
      throw new ServiceException("Role key '" + role.getRoleKey() + "' already exists");
    }

    // 2. Update role info
    SysRole updatedRole = roleRepository.save(role);

    // 3. Delete old role-menu associations
    sysRoleMenuRepository.deleteByRoleId(role.getRoleId());

    // 4. Re-insert role-menu associations
    if (role.getMenuIds() != null && role.getMenuIds().length > 0) {
      List<SysRoleMenu> roleMenus =
          Arrays.stream(role.getMenuIds())
              .map(menuId -> new SysRoleMenu(role.getRoleId(), menuId))
              .toList();
      sysRoleMenuRepository.saveAll(roleMenus);
    }

    return updatedRole;
  }

  /** Soft delete role */
  @Transactional
  public int deleteRoleById(Long roleId) {
    return roleRepository.softDeleteById(roleId);
  }

  /** Soft delete multiple roles */
  @Transactional
  public int deleteRoleByIds(Long[] roleIds) {
    //    for (Long roleId : roleIds) {
    //      checkRoleAllowed(new SysRole(roleId));
    //      checkRoleDataScope(roleId);
    //      SysRole role = selectRoleById(roleId);
    //      if (countUserRoleByRoleId(roleId) > 0) {
    //        throw new ServiceException(String.format("%1$s已分配,不能删除", role.getRoleName()));
    //      }
    //    }

    sysRoleMenuRepository.deleteByRoleIds(roleIds);
    // roleDeptMapper.deleteRoleDept(roleIds);
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
            .eq("delFlag", DelFlagEnum.NORMAL.getCode())
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
                (LocalDateTime) params.get("endTime"));

    return roleRepository.findAll(spec, pageable);
  }

  public SysRole selectRoleById(Long roleId) {
    return roleRepository.findById(roleId).orElse(null);
  }

  @Transactional
  public SysRole createRole(SysRole role) {
    role.setRoleId(null);
    role.setDelFlag(DelFlagEnum.NORMAL.getCode());

    // Uniqueness checkss
    if (roleRepository.existsByRoleName(role.getRoleName())) {
      throw new ServiceException("Role name '" + role.getRoleName() + "' already exists");
    }
    if (roleRepository.existsByRoleKey(role.getRoleKey())) {
      throw new ServiceException("Role key '" + role.getRoleKey() + "' already exists");
    }

    // Save role (generate ID)
    SysRole savedRole = roleRepository.save(role);

    // Save role-menu associations
    saveRoleMenus(savedRole);

    return savedRole;
  }

  private void saveRoleMenus(SysRole role) {
    Long[] menuIds = role.getMenuIds();
    if (menuIds == null || menuIds.length == 0) {
      return;
    }

    List<SysRoleMenu> roleMenus =
        Arrays.stream(menuIds).map(menuId -> new SysRoleMenu(role.getRoleId(), menuId)).toList();

    sysRoleMenuRepository.saveAll(roleMenus);
  }

  /**
   * Check if role name is unique.
   *
   * @param role the role entity to check
   * @return true if unique, false otherwise
   */
  public boolean checkRoleNameUnique(SysRole role) {
    return roleRepository
        .findFirstByRoleNameAndDelFlag(role.getRoleName(), DelFlagEnum.NORMAL.getCode())
        .map(
            existingRole ->
                existingRole
                    .getRoleId()
                    .equals(role.getRoleId())) // allow same role updating itself
        .orElse(true); // if no record found, it's unique
  }

  /**
   * Check if role key is unique.
   *
   * @param role the role entity to check
   * @return true if unique, false otherwise
   */
  public boolean checkRoleKeyUnique(SysRole role) {
    return roleRepository
        .findFirstByRoleKeyAndDelFlag(role.getRoleKey(), DelFlagEnum.NORMAL.getCode())
        .map(existingRole -> existingRole.getRoleId().equals(role.getRoleId()))
        .orElse(true);
  }

  /**
   * Get role permissions (role keys) for a given user.
   *
   * @param userId user ID
   * @return set of role permission strings
   */
  public Set<String> selectRolePermissionByUserId(Long userId) {
    List<SysRole> roles = roleRepository.selectRolePermissionByUserId(userId);
    Set<String> permissions = new HashSet<>();

    for (SysRole role : roles) {
      if (role != null && role.getRoleKey() != null) {
        // Split roleKey by comma (e.g., "admin,system:user:view")
        Arrays.stream(role.getRoleKey().trim().split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .forEach(permissions::add);
      }
    }

    log.debug("Resolved permissions for user {}: {}", userId, permissions);
    return permissions;
  }

  public List<SysRole> selectRoleAll() {
    return roleRepository.findAll();
  }
}
