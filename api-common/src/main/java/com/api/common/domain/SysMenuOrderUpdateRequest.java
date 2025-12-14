package com.api.common.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for updating menu display order. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysMenuOrderUpdateRequest {

  @NotNull(message = "menuId can not be null")
  private Long menuId;

  @NotNull(message = "orderNum can not be null")
  @Min(value = 0, message = "orderNum must be >= 0")
  private Integer orderNum;
}
