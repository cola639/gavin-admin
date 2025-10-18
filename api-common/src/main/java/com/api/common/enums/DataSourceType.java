package com.api.common.enums;

/** Supported data source types. */
public enum DataSourceType {
  /** Primary database (read/write). */
  MASTER,

  /** Secondary database (read-only). */
  SLAVE
}
