package com.api.system.imports.base;

import java.util.HashSet;
import java.util.Set;

/** Per-import batch context for validation and duplicate detection. */
public class ImportBatchContext {

  private final boolean updateSupport;
  private final Set<String> seenKeys = new HashSet<>();

  public ImportBatchContext(boolean updateSupport) {
    this.updateSupport = updateSupport;
  }

  public boolean isUpdateSupport() {
    return updateSupport;
  }

  public Set<String> getSeenKeys() {
    return seenKeys;
  }
}
