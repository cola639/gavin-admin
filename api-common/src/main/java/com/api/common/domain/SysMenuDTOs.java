package com.api.common.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * SysMenu DTO group.
 *
 * <p>Best practice: group request/response DTOs by module to reduce file count, while still keeping
 * each API contract clear and safe.
 */
public final class SysMenuDTOs {

  private SysMenuDTOs() {}

  /** Request DTO for updating menu display order. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class OrderUpdateRequest {

    @NotNull(message = "menuId can not be null")
    private Long menuId;

    @NotNull(message = "orderNum can not be null")
    @Min(value = 0, message = "orderNum must be >= 0")
    private Integer orderNum;
  }

  /** Response DTO for menu tree / menu list. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class MenuResponse {

    private Long menuId;
    private String menuName;
    private Long parentId;
    private Integer orderNum;

    private String path;
    private String component;
    private String query;

    private String routeName;
    private String isFrame;
    private String isCache;

    private String menuType;
    private String visible;
    private String status;
    private String perms;
    private String icon;

    /** Children for tree response. */
    private List<MenuResponse> children = new ArrayList<>();
  }

  /** Example: request DTO for creating menu (optional, add when needed). */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CreateRequest {

    @NotBlank(message = "Menu name cannot be empty")
    @Size(max = 50, message = "Menu name cannot exceed 50 characters")
    private String menuName;

    private Long parentId;

    @NotNull(message = "Order number cannot be null")
    @Min(value = 0, message = "orderNum must be >= 0")
    private Integer orderNum;

    @NotBlank(message = "Menu type cannot be empty")
    private String menuType;

    @Size(max = 200, message = "Path length cannot exceed 200 characters")
    private String path;

    @Size(max = 255, message = "Component length cannot exceed 255 characters")
    private String component;

    private String query;
    private String routeName;
    private String isFrame;
    private String isCache;

    private String visible;
    private String status;
    private String perms;
    private String icon;
  }
}
