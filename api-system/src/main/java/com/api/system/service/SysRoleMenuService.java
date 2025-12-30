package com.api.system.service;

import com.api.system.repository.SysRoleMenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysRoleMenuService {

  private final SysRoleMenuRepository repository;

  /** Check if a menu is already bound to roles */
  public boolean menuExistsInRoles(Long menuId) {
    return repository.countByMenuId(menuId) > 0;
  }

  /** Assign menus to a role (batch insert) */
  @Transactional
  public void assignMenusToRole(Long roleId, List<Long> menuIds) {
    // delete old associations
    repository.deleteByRoleId(roleId);

    // insert new ones
    //    if (menuIds != null && !menuIds.isEmpty()) {
    //      List<SysRoleMenu> roleMenus =
    //          menuIds.stream()
    //              .map(menuId -> SysRoleMenu.builder().roleId(roleId).menuId(menuId).build())
    //              .toList();
    //      repository.saveAll(roleMenus);
  }

  /** Remove role-menu associations by roleId */
  //  @Transactional
  //  public void removeByRoleId(Long roleId) {
  //    repository.deleteByRoleId(roleId);
  //  }
  //
  //  /** Remove role-menu associations by multiple roleIds */
  //  @Transactional
  //  public void removeByRoleIds(List<Long> roleIds) {
  //    repository.deleteByRoleIds(roleIds);
  //  }
  //
  //  /** Get all menuIds for a role */
  //  public List<Long> getMenuIdsByRoleId(Long roleId) {
  //    return repository.findByRoleId(roleId).stream().map(SysRoleMenu::getMenuId).toList();
  //  }
}
