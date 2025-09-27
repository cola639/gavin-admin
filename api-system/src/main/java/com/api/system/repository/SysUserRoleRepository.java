package com.api.system.repository;

import com.api.system.domain.SysUserRole;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Repository for SysUserRole entity. Provides data access methods for user-role associations. */
@Repository
public interface SysUserRoleRepository
    extends JpaRepository<SysUserRole, Long>, JpaSpecificationExecutor<SysUserRole> {

  /**
   * Delete user-role associations by userId.
   *
   * @param userId user id
   */
  void deleteByUserId(Long userId);

  /**
   * Count user-role associations by roleId.
   *
   * @param roleId role id
   * @return count of associations
   */
  long countByRoleId(Long roleId);

  /**
   * Delete associations by userId list.
   *
   * @param userIds list of user ids
   */
  void deleteByUserIdIn(List<Long> userIds);

  /**
   * Delete association by userId and roleId.
   *
   * @param userId user id
   * @param roleId role id
   */
  void deleteByUserIdAndRoleId(Long userId, Long roleId);

  /**
   * Delete associations by roleId and userIds.
   *
   * @param roleId role id
   * @param userIds list of user ids
   */
  void deleteByRoleIdAndUserIdIn(Long roleId, List<Long> userIds);
}
