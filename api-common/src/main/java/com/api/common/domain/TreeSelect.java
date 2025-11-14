package com.api.common.domain;

import com.api.common.constant.UserConstants;
import com.api.common.utils.StringUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 🌳 Generic Tree Node for frontend tree selectors.
 *
 * <p>Used to represent hierarchical data structures (e.g., departments, menus) in a
 * front-end-friendly JSON format. Each node includes: - `id` (unique identifier) - `label` (display
 * name) - `disabled` (whether selectable) - `children` (nested nodes)
 *
 * <p>Compatible with UI libraries such as Element UI Tree or Ant Design TreeSelect.
 *
 * <p>Example JSON:
 *
 * <pre>
 * {
 *   "id": 1,
 *   "label": "System Management",
 *   "disabled": false,
 *   "children": [
 *     { "id": 2, "label": "User Management" }
 *   ]
 * }
 * </pre>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TreeSelect implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /** Node ID (department ID or menu ID). */
  private Long id;

  /** Display label. */
  private String label;

  /** Whether this node is disabled (non-selectable). */
  private boolean disabled;

  /** Child nodes (optional, omitted when empty). */
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<TreeSelect> children;

  // ------------------------------------------------------------------------
  // 🏢 Constructors for Specific Domain Types
  // ------------------------------------------------------------------------

  /**
   * Builds a tree node from a {@link SysDept} entity.
   *
   * <p>Marks the node as disabled if {@code dept.status == DEPT_DISABLE}.
   */
  public TreeSelect(SysDept dept) {
    this.id = dept.getDeptId();
    this.label = dept.getDeptName();
    this.disabled = StringUtils.equals(UserConstants.DEPT_DISABLE, dept.getStatus());

    this.children =
        Optional.ofNullable(dept.getChildren()).orElse(List.of()).stream()
            .map(TreeSelect::new)
            .collect(Collectors.toList());
  }

  /** Builds a tree node from a {@link SysMenu} entity. */
  public TreeSelect(SysMenu menu) {
    this.id = menu.getMenuId();
    this.label = menu.getMenuName();

    this.children =
        Optional.ofNullable(menu.getChildren()).orElse(List.of()).stream()
            .map(TreeSelect::new)
            .collect(Collectors.toList());
  }
}
