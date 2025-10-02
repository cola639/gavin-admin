package com.api.persistence.domain.system;

import com.api.persistence.domain.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sys_menu")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SysMenu extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "menu_id")
  private Long menuId;

  @NotBlank(message = "Menu name cannot be empty")
  @Size(max = 50, message = "Menu name cannot exceed 50 characters")
  @Column(name = "menu_name")
  private String menuName;

  /** Parent menu ID */
  @Column(name = "parent_id")
  private Long parentId;

  @NotNull(message = "Order number cannot be null")
  @Column(name = "order_num")
  private Integer orderNum;

  @Size(max = 200)
  private String path;

  @Size(max = 255)
  private String component;

  private String query;

  @Column(name = "route_name")
  private String routeName;

  @Column(name = "is_frame")
  private String isFrame;

  @Column(name = "is_cache")
  private String isCache;

  @NotBlank(message = "Menu type cannot be empty")
  @Column(name = "menu_type")
  private String menuType;

  private String visible;
  private String status;
  private String perms;
  private String icon;

  /** Child menus (not persisted, used in tree building) */
  @Transient private List<SysMenu> children = new ArrayList<>();
}
