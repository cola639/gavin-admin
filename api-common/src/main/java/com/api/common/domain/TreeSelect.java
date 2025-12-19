package com.api.common.domain;

import com.api.common.constant.UserConstants;
import com.api.common.utils.StringUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TreeSelect implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  private Long id;
  private String label;
  private boolean disabled;

  // ✅ add this
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
  private Date createTime;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<TreeSelect> children;

  /** Build from SysDept */
  public TreeSelect(SysDept dept) {
    this.id = dept.getDeptId();
    this.label = dept.getDeptName();
    this.disabled = StringUtils.equals(UserConstants.DEPT_DISABLE, dept.getStatus());

    // ✅ set createTime (BaseEntity field)
    this.createTime = dept.getCreateTime();

    this.children =
        Optional.ofNullable(dept.getChildren()).orElse(List.of()).stream()
            .map(TreeSelect::new)
            .collect(Collectors.toList());
  }

  /** Build from SysMenu (optional) */
  public TreeSelect(SysMenu menu) {
    this.id = menu.getMenuId();
    this.label = menu.getMenuName();

    // optional: include menu createTime too if SysMenu extends BaseEntity
    this.createTime = menu.getCreateTime();

    this.children =
        Optional.ofNullable(menu.getChildren()).orElse(List.of()).stream()
            .map(TreeSelect::new)
            .collect(Collectors.toList());
  }
}
