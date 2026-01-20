package com.api.system.imports.base;

/** Validate row data and return validation result with context for generation. */
public interface ImportRowValidator<R, C> {
  ImportValidationResult<C> validate(R row, ImportBatchContext batchContext);
}
