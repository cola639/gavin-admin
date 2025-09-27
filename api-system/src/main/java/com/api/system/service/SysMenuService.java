package com.api.system.service;

import com.api.common.constant.Constants;
import com.api.common.constant.UserConstants;
import com.api.common.domain.entity.SysRole;
import com.api.system.domain.SysMenu;
import com.api.system.repository.SysMenuRepository;
import com.api.system.repository.SysRoleRepository;
import com.api.system.repository.SysRoleMenuRepository;
import com.api.common.utils.StringUtils;
import com.api.common.utils.jpa.SpecificationBuilder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysMenuService {

  private final SysMenuRepository sysMenuRepository;
  private final SysRoleRepository sysRoleRepository;
  private final SysRoleMenuRepository sysRoleMenuRepository;

  /** 查询所有菜单（支持动态条件） */
  public List<SysMenu> getMenuList(SysMenu menu) {
    Specification<SysMenu> spec =
        SpecificationBuilder.<SysMenu> builder()
            .like("menuName", menu.getMenuName())
            .eq("visible", menu.getVisible())
            .eq("status", menu.getStatus())
            .eq("parentId", menu.getParentId())
            .eq("menuType", menu.getMenuType());

    return sysMenuRepository.findAll(spec);
  }

  /** 构建菜单树结构 */
  public List<SysMenu> buildMenuTree(List<SysMenu> menus) {
    List<Long> ids = menus.stream().map(SysMenu::getMenuId).toList();
    List<SysMenu> roots =
        menus.stream().filter(m -> !ids.contains(m.getParentId())).collect(Collectors.toList());

    if (roots.isEmpty()) {
      roots = menus;
    }
    roots.forEach(m -> recursionFn(menus, m));
    return roots;
  }

  /** 查询单个菜单 */
  public Optional<SysMenu> getMenuById(Long menuId) {
    return sysMenuRepository.findById(menuId);
  }

  /** 新增菜单 */
  @Transactional
  public SysMenu createMenu(SysMenu menu) {
    return sysMenuRepository.save(menu);
  }

  /** 修改菜单 */
  @Transactional
  public SysMenu updateMenu(SysMenu menu) {
    return sysMenuRepository.save(menu);
  }

  /** 删除菜单 */
  @Transactional
  public void deleteMenu(Long menuId) {
    sysMenuRepository.deleteById(menuId);
  }

  // ====== 工具方法 ======
  private List<SysMenu> buildMenuHierarchy(List<SysMenu> list, Long parentId) {
    List<SysMenu> result = new ArrayList<>();
    for (SysMenu m : list) {
      if (Objects.equals(m.getParentId(), parentId)) {
        m.setChildren(buildMenuHierarchy(list, m.getMenuId()));
        result.add(m);
      }
    }
    return result;
  }

  private void recursionFn(List<SysMenu> list, SysMenu parent) {
    List<SysMenu> children =
        list.stream()
            .filter(m -> Objects.equals(m.getParentId(), parent.getMenuId()))
            .collect(Collectors.toList());
    parent.setChildren(children);
    children.forEach(child -> recursionFn(list, child));
  }

  public String innerLinkReplaceEach(String path) {
    return StringUtils.replaceEach(
        path,
        new String[] {Constants.HTTP, Constants.HTTPS, Constants.WWW, ".", ":"},
        new String[] {"", "", "", "/", "/"});
  }
}
