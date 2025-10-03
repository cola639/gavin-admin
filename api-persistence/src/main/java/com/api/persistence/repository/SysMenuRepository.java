package com.api.persistence.repository;

import com.api.persistence.domain.system.SysMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysMenuRepository
    extends JpaRepository<SysMenu, Long>, JpaSpecificationExecutor<SysMenu> {

  /**
   * Find distinct permissions by role ID.
   *
   * <p>Equivalent to MyBatis: select distinct m.perms ...
   */
  @Query(
      """
           SELECT DISTINCT m.perms
           FROM SysMenu m
           JOIN SysRoleMenu rm ON m.menuId = rm.menuId
           WHERE m.status = '0' AND rm.roleId = :roleId
           """)
  List<String> findPermsByRoleId(@Param("roleId") Long roleId);

  @Query(
      "select distinct m.perms from SysMenu m join SysRoleMenu rm on m.menuId = rm.menuId join SysUserRole ur on rm.roleId = ur.roleId join SysRole r on r.roleId = ur.roleId where r.status = '0' and m.status = '0' and ur.userId = :userId")
  List<String> findPermsByUserId(@Param("userId") Long userId);
}
