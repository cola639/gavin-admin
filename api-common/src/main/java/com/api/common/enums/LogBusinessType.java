package com.api.common.enums;

/** Business operation types. */
public enum LogBusinessType {

  /** Other operations */
  OTHER,

  /** Create new record */
  INSERT,

  /** Update existing record */
  UPDATE,

  /** Delete record */
  DELETE,

  /** Grant permissions */
  GRANT,

  /** Export data */
  EXPORT,

  /** Import data */
  IMPORT,

  /** Force logout */
  FORCE,

  /** Code generation */
  GENCODE,

  /** Clear data */
  CLEAN
}
