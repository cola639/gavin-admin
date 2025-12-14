package com.api.system.service;

import com.api.common.constant.Constants;
import com.api.common.constant.UserConstants;
import com.api.common.domain.SysMenuOrderUpdateRequest;
import com.api.common.utils.SecurityUtils;
import com.api.common.utils.StringUtils;
import com.api.common.utils.jpa.SpecificationBuilder;
import com.api.common.domain.SysMenu;
import com.api.persistence.repository.system.SysMenuRepository;
import com.api.persistence.repository.system.SysRoleMenuRepository;
import com.api.persistence.repository.system.SysRoleRepository;
import com.api.system.domain.vo.MetaVo;
import com.api.system.domain.vo.RouterVo;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysMenuService {

  private final SysMenuRepository sysMenuRepository;
  private final SysRoleRepository sysRoleRepository;
  private final SysRoleMenuRepository sysRoleMenuRepository;

  /** Query all menus with dynamic filter conditions. */
  public List<SysMenu> getMenuList(SysMenu menu) {
    Specification<SysMenu> spec =
        SpecificationBuilder.<SysMenu>builder()
            .like("menuName", menu.getMenuName())
            .eq("visible", menu.getVisible())
            .eq("status", menu.getStatus())
            .eq("parentId", menu.getParentId())
            .eq("menuType", menu.getMenuType());

    return sysMenuRepository.findAll(spec);
  }

  /** Build menu tree structure from a flat menu list. */
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

  /** Get a single menu by its ID. */
  public Optional<SysMenu> getMenuById(Long menuId) {
    return sysMenuRepository.findById(menuId);
  }

  /** Create a new menu. */
  @Transactional
  public SysMenu createMenu(SysMenu menu) {
    return sysMenuRepository.save(menu);
  }

  /** Update an existing menu. */
  @Transactional
  public SysMenu updateMenu(SysMenu menu) {
    return sysMenuRepository.save(menu);
  }

  // ====== Helper methods for menu tree building ======

  /**
   * Build menu hierarchy list starting from given parentId.
   *
   * @param list flat menu list
   * @param parentId parent menu id
   * @return hierarchical menu list
   */
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

  /**
   * Recursively build children for a given menu node.
   *
   * @param list full menu list
   * @param t current menu node
   */
  private void recursionFn(List<SysMenu> list, SysMenu t) {
    // Get child menu list
    List<SysMenu> childList = getChildList(list, t);
    t.setChildren(childList);
    for (SysMenu tChild : childList) {
      if (hasChild(list, tChild)) {
        recursionFn(list, tChild);
      }
    }
  }

  /** Normalize inner-link path (remove protocol/domain and replace with router-safe path). */
  public String innerLinkReplaceEach(String path) {
    return StringUtils.replaceEach(
        path,
        new String[] {Constants.HTTP, Constants.HTTPS, Constants.WWW, ".", ":"},
        new String[] {"", "", "", "/", "/"});
  }

  /** Insert a menu (simple wrapper around save). */
  public SysMenu insertMenu(SysMenu menu) {
    return sysMenuRepository.save(menu);
  }

  @Transactional
  public int updateMenuOrders(List<SysMenuOrderUpdateRequest> updates, String updateBy) {
    if (updates == null || updates.isEmpty()) {
      log.info("Skip updating menu orders: empty request.");
      return 0;
    }

    // Detect duplicate menuId
    Map<Long, Long> dupCheck =
        updates.stream()
            .collect(
                Collectors.groupingBy(SysMenuOrderUpdateRequest::getMenuId, Collectors.counting()));
    List<Long> duplicated =
        dupCheck.entrySet().stream()
            .filter(e -> e.getKey() != null && e.getValue() != null && e.getValue() > 1)
            .map(Map.Entry::getKey)
            .toList();
    if (!duplicated.isEmpty()) {
      throw new IllegalArgumentException("Duplicate menuId in request: " + duplicated);
    }

    Map<Long, Integer> orderMap =
        updates.stream()
            .collect(
                Collectors.toMap(
                    SysMenuOrderUpdateRequest::getMenuId, SysMenuOrderUpdateRequest::getOrderNum));

    List<Long> menuIds = updates.stream().map(SysMenuOrderUpdateRequest::getMenuId).toList();
    List<SysMenu> menus = sysMenuRepository.findAllById(menuIds);

    if (menus.size() != menuIds.size()) {
      Set<Long> found = menus.stream().map(SysMenu::getMenuId).collect(Collectors.toSet());
      List<Long> missing = menuIds.stream().filter(id -> !found.contains(id)).distinct().toList();
      throw new EntityNotFoundException("Menu not found: " + missing);
    }

    menus.forEach(
        m -> {
          m.setOrderNum(orderMap.get(m.getMenuId()));
          m.setUpdateBy(updateBy);
        });

    sysMenuRepository.saveAll(menus);
    sysMenuRepository.flush();

    log.info("Updated menu order successfully. size={}", menus.size());
    return menus.size();
  }

  /** Delete a menu by id. */
  @Transactional
  public void deleteMenuById(Long menuId) {
    if (!sysMenuRepository.existsById(menuId)) {
      throw new EntityNotFoundException("Menu does not exist: " + menuId);
    }
    sysMenuRepository.deleteById(menuId);
  }

  /** Get menu permissions by role ID. */
  public Set<String> selectMenuPermsByRoleId(Long roleId) {
    List<String> perms = sysMenuRepository.findPermsByRoleId(roleId);
    return toPermSet(perms);
  }

  /** Get menu permissions by user ID. */
  public Set<String> selectMenuPermsByUserId(Long userId) {
    List<String> perms = sysMenuRepository.findPermsByUserId(userId);
    return toPermSet(perms);
  }

  /** Convert list of comma-separated permissions to a clean Set. */
  private Set<String> toPermSet(List<String> perms) {
    Set<String> permsSet = new HashSet<>();
    for (String perm : perms) {
      if (perm != null && !perm.isBlank()) {
        permsSet.addAll(Arrays.asList(perm.trim().split(",")));
      }
    }
    return permsSet;
  }

  /**
   * Select menu tree by user id, respecting visibility and role permissions.
   *
   * @param userId user id
   * @return menu tree for the user
   */
  public List<SysMenu> selectMenuTreeByUserId(Long userId) {
    List<SysMenu> menus =
        SecurityUtils.isAdmin(userId)
            ? sysMenuRepository.findAllVisibleMenus()
            : sysMenuRepository.findMenusByUserId(userId);

    return getChildPerms(menus, 0L);
  }

  /**
   * Recursively build menu tree from flat list using Long parentId.
   *
   * @param menus menu list
   * @param parentId parent menu id
   * @return hierarchical menu list
   */
  private List<SysMenu> getChildPerms(List<SysMenu> menus, Long parentId) {
    return menus.stream()
        .filter(menu -> parentId.equals(menu.getParentId()))
        .peek(menu -> menu.setChildren(getChildPerms(menus, menu.getMenuId())))
        .toList();
  }

  /**
   * Build frontend router definitions from menu tree.
   *
   * @param menus menu tree
   * @return router list for frontend
   */
  public List<RouterVo> buildMenus(List<SysMenu> menus) {
    List<RouterVo> routers = new LinkedList<RouterVo>();
    for (SysMenu menu : menus) {
      RouterVo router = new RouterVo();
      router.setHidden("1".equals(menu.getVisible()));
      router.setName(getRouteName(menu));
      router.setPath(getRouterPath(menu));
      router.setComponent(getComponent(menu));
      router.setQuery(menu.getQuery());
      router.setMeta(
          new MetaVo(
              menu.getMenuName(),
              menu.getIcon(),
              StringUtils.equals("1", menu.getIsCache()),
              menu.getPath()));
      List<SysMenu> cMenus = menu.getChildren();
      if (StringUtils.isNotEmpty(cMenus) && UserConstants.TYPE_DIR.equals(menu.getMenuType())) {
        // Directory with children
        router.setAlwaysShow(true);
        router.setRedirect("noRedirect");
        router.setChildren(buildMenus(cMenus));
      } else if (isMenuFrame(menu)) {
        // Menu frame (single-level menu that opens sub-route)
        router.setMeta(null);
        List<RouterVo> childrenList = new ArrayList<RouterVo>();
        RouterVo children = new RouterVo();
        children.setPath(menu.getPath());
        children.setComponent(menu.getComponent());
        children.setName(getRouteName(menu.getRouteName(), menu.getPath()));
        children.setMeta(
            new MetaVo(
                menu.getMenuName(),
                menu.getIcon(),
                StringUtils.equals("1", menu.getIsCache()),
                menu.getPath()));
        children.setQuery(menu.getQuery());
        childrenList.add(children);
        router.setChildren(childrenList);
      } else if (menu.getParentId().intValue() == 0 && isInnerLink(menu)) {
        // Top-level inner link (open external link via inner-link component)
        router.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon()));
        router.setPath("/");
        List<RouterVo> childrenList = new ArrayList<RouterVo>();
        RouterVo children = new RouterVo();
        String routerPath = innerLinkReplaceEach(menu.getPath());
        children.setPath(routerPath);
        children.setComponent(UserConstants.INNER_LINK);
        children.setName(getRouteName(menu.getRouteName(), routerPath));
        children.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon(), menu.getPath()));
        childrenList.add(children);
        router.setChildren(childrenList);
      }
      routers.add(router);
    }
    return routers;
  }

  /**
   * Check menu name uniqueness under the same parent.
   *
   * @param menu menu to check
   * @return result flag (UNIQUE / NOT_UNIQUE)
   */
  //    public boolean checkMenuNameUnique(SysMenu menu) {
  //      Long menuId = StringUtils.isNull(menu.getMenuId()) ? -1L : menu.getMenuId();
  //      SysMenu info = menuMapper.checkMenuNameUnique(menu.getMenuName(), menu.getParentId());
  //      if (StringUtils.isNotNull(info) && info.getMenuId().longValue() != menuId.longValue()) {
  //        return UserConstants.NOT_UNIQUE;
  //      }
  //      return UserConstants.UNIQUE;
  //    }

  public boolean hasChildByMenuId(Long getMenuId) {
    return sysMenuRepository.existsByParentId(getMenuId);
  }

  public boolean isMenuAssignedToAnyRole(Long menuId) {
    return sysRoleMenuRepository.existsByMenuId(menuId);
  }

  /**
   * Get route name for frontend router.
   *
   * @param menu menu info
   * @return route name
   */
  public String getRouteName(SysMenu menu) {
    // Non-outer-link and top-level directory (menu type = menu)
    if (isMenuFrame(menu)) {
      return StringUtils.EMPTY;
    }
    return getRouteName(menu.getRouteName(), menu.getPath());
  }

  /**
   * Get route name. If no explicit name provided, use path.
   *
   * @param name route name
   * @param path route path
   * @return route name (capitalized)
   */
  public String getRouteName(String name, String path) {
    String routerName = StringUtils.isNotEmpty(name) ? name : path;
    return StringUtils.capitalize(routerName);
  }

  /**
   * Get router path for frontend route.
   *
   * @param menu menu info
   * @return router path
   */
  public String getRouterPath(SysMenu menu) {
    String routerPath = menu.getPath();
    // Inner link opened as external link
    if (menu.getParentId().intValue() != 0 && isInnerLink(menu)) {
      routerPath = innerLinkReplaceEach(routerPath);
    }
    // Non-outer-link and top-level directory (type = DIR)
    if (0 == menu.getParentId().intValue()
        && UserConstants.TYPE_DIR.equals(menu.getMenuType())
        && UserConstants.NO_FRAME.equals(menu.getIsFrame())) {
      routerPath = "/" + menu.getPath();
    }
    // Non-outer-link and top-level menu (type = MENU)
    else if (isMenuFrame(menu)) {
      routerPath = "/";
    }
    return routerPath;
  }

  /**
   * Get component name for frontend router.
   *
   * @param menu menu info
   * @return component name
   */
  public String getComponent(SysMenu menu) {
    String component = UserConstants.LAYOUT;
    if (StringUtils.isNotEmpty(menu.getComponent()) && !isMenuFrame(menu)) {
      component = menu.getComponent();
    } else if (StringUtils.isEmpty(menu.getComponent())
        && menu.getParentId().intValue() != 0
        && isInnerLink(menu)) {
      component = UserConstants.INNER_LINK;
    } else if (StringUtils.isEmpty(menu.getComponent()) && isParentView(menu)) {
      component = UserConstants.PARENT_VIEW;
    }
    return component;
  }

  /**
   * Check whether the menu is a “menu frame”.
   *
   * <p>Definition: top-level menu (parentId = 0), type = MENU, isFrame = NO_FRAME.
   *
   * @param menu menu info
   * @return true if menu is a frame menu
   */
  public boolean isMenuFrame(SysMenu menu) {
    return menu.getParentId().intValue() == 0
        && UserConstants.TYPE_MENU.equals(menu.getMenuType())
        && menu.getIsFrame().equals(UserConstants.NO_FRAME);
  }

  /**
   * Check whether the menu is an inner-link component.
   *
   * @param menu menu info
   * @return true if menu is an inner-link
   */
  public boolean isInnerLink(SysMenu menu) {
    return menu.getIsFrame().equals(UserConstants.NO_FRAME) && StringUtils.isHttp(menu.getPath());
  }

  /**
   * Check whether the menu should use parent-view component.
   *
   * @param menu menu info
   * @return true if menu is parent-view
   */
  public boolean isParentView(SysMenu menu) {
    return menu.getParentId().intValue() != 0 && UserConstants.TYPE_DIR.equals(menu.getMenuType());
  }

  /**
   * Build menu tree for authorization based on parentId (int).
   *
   * @param list full menu list
   * @param parentId parent menu id
   * @return menu tree
   */
  public List<SysMenu> getChildPerms(List<SysMenu> list, int parentId) {
    List<SysMenu> returnList = new ArrayList<SysMenu>();
    for (Iterator<SysMenu> iterator = list.iterator(); iterator.hasNext(); ) {
      SysMenu t = iterator.next();
      // For a given parentId, find all child nodes
      if (t.getParentId() == parentId) {
        recursionFn(list, t);
        returnList.add(t);
      }
    }
    return returnList;
  }

  /**
   * Get direct child menus of a given menu.
   *
   * @param list menu list
   * @param t parent menu
   * @return child menus of t
   */
  private List<SysMenu> getChildList(List<SysMenu> list, SysMenu t) {
    List<SysMenu> tlist = new ArrayList<SysMenu>();
    Iterator<SysMenu> it = list.iterator();
    while (it.hasNext()) {
      SysMenu n = it.next();
      if (n.getParentId().longValue() == t.getMenuId().longValue()) {
        tlist.add(n);
      }
    }
    return tlist;
  }

  /**
   * Check whether a menu has child nodes.
   *
   * @param list menu list
   * @param t menu node
   * @return true if menu has children
   */
  private boolean hasChild(List<SysMenu> list, SysMenu t) {
    return getChildList(list, t).size() > 0;
  }
}
