package com.api.system.imports.user;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Row-level error information for import responses. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserImportRowError {

  private int rowIndex;
  private String userName;
  private List<String> messages;
}
