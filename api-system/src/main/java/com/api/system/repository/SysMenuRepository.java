package com.api.system.repository;

import com.api.system.domain.SysMenu;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SysMenuRepository
    extends JpaRepository<SysMenu, Long>, JpaSpecificationExecutor<SysMenu> {

  /** Get all permissions */
  @Query("select distinct m.perms from SysMenu m where m.perms is not null and m.perms <> ''")
  List<String> findAllPerms();

  /** Get menu list by userId (join sys_user_role → sys_prole_menu → sys_menu) */
  @Query(
      """
            select distinct m from SysMenu m
            join SysRoleMenu rm on rm.menuId = m.menuId
            join SysUserRole ur on ur.roleId = rm.roleId
            where ur.userId = :userId and m.status = '0'
            """)
  List<SysMenu> findMenuTreeByUserId(@Param("userId") Long userId);

  /** Get perms by roleId */
  @Query(
      """
              select distinct m.perms from SysMenu m
              join SysRoleMenu rm on rm.menuId = m.menuId
              where rm.roleId = :roleId and m.status = '0'
              """)
  List<String> findPermsByRoleId(@Param("roleId") Long roleId);

  /** Check menu name unique under the same parent */
  Optional<SysMenu> findFirstByMenuNameAndParentId(String menuName, Long parentId);

  /** Get children menus by parentId */
  List<SysMenu> findByParentId(Long parentId);
}
