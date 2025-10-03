package com.api.persistence.repository;

import com.api.persistence.domain.common.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysRoleRepository
    extends JpaRepository<SysRole, Long>, JpaSpecificationExecutor<SysRole> {

  /** Check unique role name */
  Optional<SysRole> findFirstByRoleNameAndDelFlag(String roleName, String delFlag);

  /** Check unique role key */
  Optional<SysRole> findFirstByRoleKeyAndDelFlag(String roleKey, String delFlag);

  /** Find roles by username (join sys_user and sys_user_role) */
  @Query(
      """
                select r from SysRole r
                join SysUserRole ur on ur.roleId = r.roleId
                join SysUser u on u.userId = ur.userId
                where r.delFlag = '0' and u.userName = :userName
            """)
  List<SysRole> findRolesByUserName(@Param("userName") String userName);

  /** Find roles by userId */
  @Query(
      """
                select r from SysRole r
                join SysUserRole ur on ur.roleId = r.roleId
                where ur.userId = :userId
            """)
  List<SysRole> findRolesByUserId(@Param("userId") Long userId);

  /** Count roles by status */
  long countByStatus(String status);

  /** Soft delete by roleId */
  @Modifying
  @Query("update SysRole r set r.delFlag = '2' where r.roleId = :roleId")
  int softDeleteById(@Param("roleId") Long roleId);

  /** Soft delete by roleIds */
  @Modifying
  @Query("update SysRole r set r.delFlag = '2' where r.roleId in :roleIds")
  int softDeleteByIds(@Param("roleIds") Long[] roleIds);

  @Query(
      """
        select distinct r from SysRole r
        join SysUser u on u.userId = :userId
        join u.roles ur
        where r.delFlag = '0'
    """)
  List<SysRole> selectRolePermissionByUserId(Long userId);
}
