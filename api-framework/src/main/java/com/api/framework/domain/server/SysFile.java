package com.api.framework.domain.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * Represents system disk or file storage information.
 *
 * <p>This class contains information about a file system, such as mount point, type, capacity, and
 * usage.
 *
 * <p>Design principles: - Uses Lombok for cleaner and shorter code. - Uses Jackson for JSON
 * serialization. - Uses @Slf4j for debugging and diagnostics. - Non-persistent: runtime DTO only. -
 * JDK 8 compatible.
 */
@Slf4j
@Data
public class SysFile implements Serializable {

  private static final long serialVersionUID = 1L;

  /** Mount path (e.g., "C:\\" or "/home"). */
  @JsonProperty("dirName")
  private String dirName;

  /** System type name (e.g., NTFS, ext4). */
  @JsonProperty("sysTypeName")
  private String sysTypeName;

  /** File type name (e.g., local disk, NFS, virtual). */
  @JsonProperty("typeName")
  private String typeName;

  /** Total disk space (formatted string, e.g., "120.5 GB"). */
  @JsonProperty("total")
  private String total;

  /** Free disk space (formatted string). */
  @JsonProperty("free")
  private String free;

  /** Used disk space (formatted string). */
  @JsonProperty("used")
  private String used;

  /** Usage percentage (e.g., 73.4). */
  @JsonProperty("usagePercent")
  private double usage;

  /** Logs this file system info for diagnostics. */
  public void logDiskInfo() {
    log.debug(
        "Disk [{}]: Type={}, Used={}, Free={}, Usage={}%", dirName, sysTypeName, used, free, usage);
  }
}
