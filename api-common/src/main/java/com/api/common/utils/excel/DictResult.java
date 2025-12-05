package com.api.common.utils.excel;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DictResult {
  private String label; // display text
  private String color; // hex color string, e.g. "#FF0000"
}
