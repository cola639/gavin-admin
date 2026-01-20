package com.api.system.imports.user;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Summary response for a user import. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserImportResult {

  private int totalRows;
  private int successCount;
  private int createdCount;
  private int updatedCount;
  private int errorCount;
  private List<UserImportRowError> rowErrors;
}
