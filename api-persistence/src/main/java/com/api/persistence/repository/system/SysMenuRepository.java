package com.api.persistence.repository.system;

import com.api.common.domain.SysMenu;
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
           WHERE m.status = 'Enabled' AND rm.roleId = :roleId
           """)
  List<String> findPermsByRoleId(@Param("roleId") Long roleId);

  @Query(
      "select distinct m.perms from SysMenu m join SysRoleMenu rm on m.menuId = rm.menuId join SysUserRole ur on rm.roleId = ur.roleId join SysRole r on r.roleId = ur.roleId where r.status = '0' and m.status = '0' and ur.userId = :userId")
  List<String> findPermsByUserId(@Param("userId") Long userId);

  /** Get all menus visible in the system */
  @Query(
      """
                  SELECT DISTINCT m
                  FROM SysMenu m
                  WHERE m.menuType IN ('Menu', 'Module')
                    AND m.status = 'Enabled'
                  ORDER BY m.parentId, m.orderNum
              """)
  List<SysMenu> findAllVisibleMenus();

  /** Get menus accessible by a specific user */
  @Query(
      """
              SELECT DISTINCT m
              FROM SysUserRole ur
              JOIN SysRoleMenu rm ON ur.roleId = rm.roleId
              JOIN SysMenu m ON rm.menuId = m.menuId
              JOIN SysRole r ON r.roleId = ur.roleId
              WHERE ur.userId = :userId
                AND r.status = 'Enabled'
                AND m.status = 'Enabled'
              ORDER BY m.parentId, m.orderNum
              """)
  List<SysMenu> findMenusByUserId(@Param("userId") Long userId);

  @Query(
      """
      SELECT DISTINCT m.menuId
      FROM SysMenu m
      JOIN SysRoleMenu rm ON m.menuId = rm.menuId
      WHERE rm.roleId = :roleId
      """)
  List<Long> findMenuIdsByRoleId(@Param("roleId") Long roleId);

  boolean existsByParentId(Long parentId);
}
