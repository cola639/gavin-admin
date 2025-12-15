package com.api.common.domain;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * SysMenu mapper.
 *
 * <p>Use a mapper to avoid exposing entities directly to controller/front-end and to keep
 * transformation logic centralized.
 */
public interface SysMenuMapper {

  /** Convert entity -> response DTO (recursive for children). */
  static SysMenuDTOs.MenuResponse toResponse(SysMenu entity) {
    if (entity == null) {
      return null;
    }

    SysMenuDTOs.MenuResponse resp = new SysMenuDTOs.MenuResponse();
    resp.setMenuId(entity.getMenuId());
    resp.setMenuName(entity.getMenuName());
    resp.setParentId(entity.getParentId());
    resp.setOrderNum(entity.getOrderNum());

    resp.setPath(entity.getPath());
    resp.setComponent(entity.getComponent());
    resp.setQuery(entity.getQuery());

    resp.setRouteName(entity.getRouteName());
    resp.setIsFrame(entity.getIsFrame());
    resp.setIsCache(entity.getIsCache());

    resp.setMenuType(entity.getMenuType());
    resp.setVisible(entity.getVisible());
    resp.setStatus(entity.getStatus());
    resp.setPerms(entity.getPerms());
    resp.setIcon(entity.getIcon());

    if (entity.getChildren() != null && !entity.getChildren().isEmpty()) {
      List<SysMenuDTOs.MenuResponse> children =
          entity.getChildren().stream()
              .filter(Objects::nonNull)
              .map(SysMenuMapper::toResponse)
              .collect(Collectors.toList());
      resp.setChildren(children);
    } else {
      resp.setChildren(Collections.emptyList());
    }

    return resp;
  }

  /** Convert entity list -> response DTO list. */
  static List<SysMenuDTOs.MenuResponse> toResponses(List<SysMenu> entities) {
    if (entities == null || entities.isEmpty()) {
      return Collections.emptyList();
    }
    return entities.stream()
        .filter(Objects::nonNull)
        .map(SysMenuMapper::toResponse)
        .collect(Collectors.toList());
  }

  /**
   * Apply order update request -> entity (partial update).
   *
   * <p>Note: caller should load the entity from DB first, then apply.
   */
  static void applyOrderUpdate(SysMenuDTOs.OrderUpdateRequest req, SysMenu entity) {
    if (req == null || entity == null) {
      return;
    }
    entity.setOrderNum(req.getOrderNum());
  }
}
