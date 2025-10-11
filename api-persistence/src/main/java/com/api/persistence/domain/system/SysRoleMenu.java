package com.api.persistence.domain.system;

import com.api.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Objects;

/** Role-Menu association entity using composite key (roleId + menuId). */
@Entity
@Table(name = "sys_role_menu")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@IdClass(SysRoleMenu.CompositeKey.class) // define composite key inside this class
public class SysRoleMenu extends BaseEntity {
  /** Custom constructor for convenience */
  @Id
  @Column(name = "role_id")
  private Long roleId;

  @Id
  @Column(name = "menu_id")
  private Long menuId;

  /** Inner static class representing composite primary key. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CompositeKey implements Serializable {
    private Long roleId;
    private Long menuId;

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof CompositeKey that)) return false;
      return Objects.equals(roleId, that.roleId) && Objects.equals(menuId, that.menuId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(roleId, menuId);
    }
  }
}
