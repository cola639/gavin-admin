package com.api.system.repository;

import com.api.system.domain.SysRoleMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SysRoleMenuRepository
    extends JpaRepository<SysRoleMenu, SysRoleMenu.CompositeKey> {

  /** Check if any role uses a given menu */
  long countByMenuId(Long menuId);

  /** Delete by roleId */
  @Transactional
  @Modifying
  @Query("delete from SysRoleMenu rm where rm.roleId = :roleId")
  int deleteByRoleId(Long roleId);

  /** Delete by roleIds */
  @Transactional
  @Modifying
  @Query("delete from SysRoleMenu rm where rm.roleId in :roleIds")
  int deleteByRoleIds(List<Long> roleIds);

  /** Delete by roleId and menuId */
  @Transactional
  int deleteByRoleIdAndMenuId(Long roleId, Long menuId);

  /** Find menus by roleId */
  List<SysRoleMenu> findByRoleId(Long roleId);
}
