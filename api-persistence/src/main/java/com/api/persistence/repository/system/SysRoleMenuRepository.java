package com.api.persistence.repository.system;

import com.api.persistence.domain.system.SysRoleMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SysRoleMenuRepository
    extends JpaRepository<SysRoleMenu, SysRoleMenu.CompositeKey> {

  /** Check if any role uses a given menu */
  long countByMenuId(Long menuId);

  /** Delete by roleId */
  @Modifying
  @Query("delete from SysRoleMenu rm where rm.roleId = :roleId")
  int deleteByRoleId(Long roleId);

  /** Delete by roleIds */
  @Modifying
  @Query("delete from SysRoleMenu rm where rm.roleId in :roleIds")
  int deleteByRoleIds(Long[] roleIds);

  /** Delete by roleId and menuId */
  int deleteByRoleIdAndMenuId(Long roleId, Long menuId);

  /** Find menus by roleId */
  List<SysRoleMenu> findByRoleId(Long roleId);

  boolean existsByMenuId(Long menuId);
}
