package com.api.system.imports.base;

import com.api.common.utils.excel.ExcelRow;

/** Map Excel rows into typed row objects (no business rules). */
public interface ImportRowMapper<R> {
  R map(ExcelRow row);
}
