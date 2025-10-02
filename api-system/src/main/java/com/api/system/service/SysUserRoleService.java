package com.api.system.service;

import com.api.persistence.domain.system.SysUserRole;
import com.api.persistence.repository.SysUserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Service for SysUserRole. */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserRoleService {
  private final SysUserRoleRepository sysUserRoleRepository;

  /**
   * Insert user-role associations.
   *
   * @param userId user id
   * @param roleIds role ids
   */
  public void insertUserRole(Long userId, Long[] roleIds) {
    if (roleIds != null && roleIds.length > 0) {
      List<SysUserRole> list = new ArrayList<>(roleIds.length);
      for (Long roleId : roleIds) {
        SysUserRole ur = SysUserRole.builder().userId(userId).roleId(roleId).build();
        list.add(ur);
      }
      sysUserRoleRepository.saveAll(list);
      log.info("Inserted {} user-role associations for userId={}", list.size(), userId);
    } else {
      log.info("No roles to insert for userId={}", userId);
    }
  }

  public void deleteByUserId(Long userId) {
    log.info("Deleting user-role associations by userId: {}", userId);
    sysUserRoleRepository.deleteByUserId(userId);
  }

  public long countByRoleId(Long roleId) {
    log.info("Counting user-role associations by roleId: {}", roleId);
    return sysUserRoleRepository.countByRoleId(roleId);
  }

  public void deleteByUserIds(List<Long> userIds) {
    log.info("Deleting user-role associations by userIds: {}", userIds);
    sysUserRoleRepository.deleteByUserIdIn(userIds);
  }

  public void saveAll(List<SysUserRole> userRoles) {
    log.info("Saving {} user-role associations", userRoles.size());
    sysUserRoleRepository.saveAll(userRoles);
  }

  public void deleteByUserIdAndRoleId(Long userId, Long roleId) {
    log.info("Deleting user-role association userId={} and roleId={}", userId, roleId);
    sysUserRoleRepository.deleteByUserIdAndRoleId(userId, roleId);
  }

  public void deleteByRoleIdAndUserIds(Long roleId, List<Long> userIds) {
    log.info("Deleting user-role associations by roleId={} and userIds={}", roleId, userIds);
    sysUserRoleRepository.deleteByRoleIdAndUserIdIn(roleId, userIds);
  }

  public List<SysUserRole> findAll(Specification<SysUserRole> spec) {
    log.info("Finding user-role associations with specification");
    return sysUserRoleRepository.findAll(spec);
  }
}
