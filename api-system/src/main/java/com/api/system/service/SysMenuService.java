package com.api.system.service;

import com.api.common.constant.Constants;
import com.api.common.constant.UserConstants;
import com.api.common.utils.SecurityUtils;
import com.api.common.utils.StringUtils;
import com.api.common.utils.jpa.SpecificationBuilder;
import com.api.persistence.domain.system.SysMenu;
import com.api.persistence.repository.system.SysMenuRepository;
import com.api.persistence.repository.system.SysRoleMenuRepository;
import com.api.persistence.repository.system.SysRoleRepository;
import com.api.system.domain.vo.MetaVo;
import com.api.system.domain.vo.RouterVo;
import jakarta.persistence.EntityNotFoundException;
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
        SpecificationBuilder.<SysMenu>builder()
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

  private void recursionFn(List<SysMenu> list, SysMenu t) {
    // 得到子节点列表
    List<SysMenu> childList = getChildList(list, t);
    t.setChildren(childList);
    for (SysMenu tChild : childList) {
      if (hasChild(list, tChild)) {
        recursionFn(list, tChild);
      }
    }
  }

  public String innerLinkReplaceEach(String path) {
    return StringUtils.replaceEach(
        path,
        new String[] {Constants.HTTP, Constants.HTTPS, Constants.WWW, ".", ":"},
        new String[] {"", "", "", "/", "/"});
  }

  public SysMenu insertMenu(SysMenu menu) {
    return sysMenuRepository.save(menu);
  }

  /** Delete Menu */
  @Transactional
  public void deleteMenuById(Long menuId) {
    if (!sysMenuRepository.existsById(menuId)) {
      throw new EntityNotFoundException("Menu is not existed" + menuId);
    }
    sysMenuRepository.deleteById(menuId);
  }

  /** Get permissions by role ID. */
  public Set<String> selectMenuPermsByRoleId(Long roleId) {
    List<String> perms = sysMenuRepository.findPermsByRoleId(roleId);
    return toPermSet(perms);
  }

  /** Get permissions by user ID. */
  public Set<String> selectMenuPermsByUserId(Long userId) {
    List<String> perms = sysMenuRepository.findPermsByUserId(userId);
    return toPermSet(perms);
  }

  /** Convert list of comma-separated permissions to a clean set. */
  private Set<String> toPermSet(List<String> perms) {
    Set<String> permsSet = new HashSet<>();
    for (String perm : perms) {
      if (perm != null && !perm.isBlank()) {
        permsSet.addAll(Arrays.asList(perm.trim().split(",")));
      }
    }
    return permsSet;
  }

  public List<SysMenu> selectMenuTreeByUserId(Long userId) {
    List<SysMenu> menus =
        SecurityUtils.isAdmin(userId)
            ? sysMenuRepository.findAllVisibleMenus()
            : sysMenuRepository.findMenusByUserId(userId);

    return getChildPerms(menus, 0L);
  }

  /** Recursively build menu tree */
  private List<SysMenu> getChildPerms(List<SysMenu> menus, Long parentId) {
    return menus.stream()
        .filter(menu -> parentId.equals(menu.getParentId()))
        .peek(menu -> menu.setChildren(getChildPerms(menus, menu.getMenuId())))
        .toList();
  }

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
        router.setAlwaysShow(true);
        router.setRedirect("noRedirect");
        router.setChildren(buildMenus(cMenus));
      } else if (isMenuFrame(menu)) {
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

  //  public boolean checkMenuNameUnique(SysMenu menu) {
  //    Long menuId = StringUtils.isNull(menu.getMenuId()) ? -1L : menu.getMenuId();
  //    SysMenu info = menuMapper.checkMenuNameUnique(menu.getMenuName(), menu.getParentId());
  //    if (StringUtils.isNotNull(info) && info.getMenuId().longValue() != menuId.longValue()) {
  //      return UserConstants.NOT_UNIQUE;
  //    }
  //    return UserConstants.UNIQUE;
  //  }

  /**
   * 获取路由名称
   *
   * @param menu 菜单信息
   * @return 路由名称
   */
  public String getRouteName(SysMenu menu) {
    // 非外链并且是一级目录（类型为目录）
    if (isMenuFrame(menu)) {
      return StringUtils.EMPTY;
    }
    return getRouteName(menu.getRouteName(), menu.getPath());
  }

  /**
   * 获取路由名称，如没有配置路由名称则取路由地址
   *
   * @param name 路由名称
   * @param path 路由地址
   * @return 路由名称（驼峰格式）
   */
  public String getRouteName(String name, String path) {
    String routerName = StringUtils.isNotEmpty(name) ? name : path;
    return StringUtils.capitalize(routerName);
  }

  /**
   * 获取路由地址
   *
   * @param menu 菜单信息
   * @return 路由地址
   */
  public String getRouterPath(SysMenu menu) {
    String routerPath = menu.getPath();
    // 内链打开外网方式
    if (menu.getParentId().intValue() != 0 && isInnerLink(menu)) {
      routerPath = innerLinkReplaceEach(routerPath);
    }
    // 非外链并且是一级目录（类型为目录）
    if (0 == menu.getParentId().intValue()
        && UserConstants.TYPE_DIR.equals(menu.getMenuType())
        && UserConstants.NO_FRAME.equals(menu.getIsFrame())) {
      routerPath = "/" + menu.getPath();
    }
    // 非外链并且是一级目录（类型为菜单）
    else if (isMenuFrame(menu)) {
      routerPath = "/";
    }
    return routerPath;
  }

  /**
   * 获取组件信息
   *
   * @param menu 菜单信息
   * @return 组件信息
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
   * 是否为菜单内部跳转
   *
   * @param menu 菜单信息
   * @return 结果
   */
  public boolean isMenuFrame(SysMenu menu) {
    return menu.getParentId().intValue() == 0
        && UserConstants.TYPE_MENU.equals(menu.getMenuType())
        && menu.getIsFrame().equals(UserConstants.NO_FRAME);
  }

  /**
   * 是否为内链组件
   *
   * @param menu 菜单信息
   * @return 结果
   */
  public boolean isInnerLink(SysMenu menu) {
    return menu.getIsFrame().equals(UserConstants.NO_FRAME) && StringUtils.isHttp(menu.getPath());
  }

  /**
   * 是否为parent_view组件
   *
   * @param menu 菜单信息
   * @return 结果
   */
  public boolean isParentView(SysMenu menu) {
    return menu.getParentId().intValue() != 0 && UserConstants.TYPE_DIR.equals(menu.getMenuType());
  }

  /**
   * 根据父节点的ID获取所有子节点
   *
   * @param list 分类表
   * @param parentId 传入的父节点ID
   * @return String
   */
  public List<SysMenu> getChildPerms(List<SysMenu> list, int parentId) {
    List<SysMenu> returnList = new ArrayList<SysMenu>();
    for (Iterator<SysMenu> iterator = list.iterator(); iterator.hasNext(); ) {
      SysMenu t = (SysMenu) iterator.next();
      // 一、根据传入的某个父节点ID,遍历该父节点的所有子节点
      if (t.getParentId() == parentId) {
        recursionFn(list, t);
        returnList.add(t);
      }
    }
    return returnList;
  }

  /** 得到子节点列表 */
  private List<SysMenu> getChildList(List<SysMenu> list, SysMenu t) {
    List<SysMenu> tlist = new ArrayList<SysMenu>();
    Iterator<SysMenu> it = list.iterator();
    while (it.hasNext()) {
      SysMenu n = (SysMenu) it.next();
      if (n.getParentId().longValue() == t.getMenuId().longValue()) {
        tlist.add(n);
      }
    }
    return tlist;
  }

  /** 判断是否有子节点 */
  private boolean hasChild(List<SysMenu> list, SysMenu t) {
    return getChildList(list, t).size() > 0;
  }
}
