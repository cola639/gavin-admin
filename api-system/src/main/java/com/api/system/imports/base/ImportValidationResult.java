package com.api.system.imports.base;

import java.util.List;

/** Validation result holding errors and downstream context. */
public class ImportValidationResult<C> {

  private final List<String> errors;
  private final C context;

  public ImportValidationResult(List<String> errors, C context) {
    this.errors = errors;
    this.context = context;
  }

  public List<String> getErrors() {
    return errors;
  }

  public C getContext() {
    return context;
  }

  public boolean isValid() {
    return errors == null || errors.isEmpty();
  }
}
