package com.api.system.repository;

import com.api.system.domain.system.SysUserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Repository for SysUserRole entity. Provides data access methods for user-role associations. */
@Repository
public interface SysUserRoleRepository
    extends JpaRepository<SysUserRole, SysUserRole.SysUserRoleId>,
        JpaSpecificationExecutor<SysUserRole> {

  /** Delete user-role associations by userId. Returns affected rows. */
  @Transactional
  long deleteByUserId(Long userId);

  /** Count associations by roleId. */
  long countByRoleId(Long roleId);

  /** Delete associations by a list of userIds. Returns affected rows. */
  @Transactional
  long deleteByUserIdIn(List<Long> userIds);

  /** Delete association by (userId, roleId). Returns affected rows. */
  @Transactional
  long deleteByUserIdAndRoleId(Long userId, Long roleId);

  /** Delete associations by roleId + userIds. Returns affected rows. */
  @Transactional
  long deleteByRoleIdAndUserIdIn(Long roleId, List<Long> userIds);

  /** For batch-assign: find which userIds already have this role (avoid duplicate insert). */
  @Query(
      """
      select ur.userId
      from SysUserRole ur
      where ur.roleId = :roleId
        and ur.userId in :userIds
  """)
  List<Long> findExistingUserIds(
      @Param("roleId") Long roleId, @Param("userIds") List<Long> userIds);
}
