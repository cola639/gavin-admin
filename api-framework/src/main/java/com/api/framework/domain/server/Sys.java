package com.api.framework.domain.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * Represents operating system and environment information.
 *
 * <p>This class is used to transfer real-time system metadata, including server name, IP, user
 * directory, OS name, and architecture.
 *
 * <p>Design principles: - Uses Lombok to remove boilerplate code. - Uses Jackson for clean JSON
 * serialization. - Uses @Slf4j for logging and diagnostics. - Does not persist to database. - Fully
 * compatible with JDK 8.
 */
@Slf4j
@Data
public class Sys implements Serializable {

  private static final long serialVersionUID = 1L;

  /** Server hostname. */
  @JsonProperty("computerName")
  private String computerName;

  /** Server IP address. */
  @JsonProperty("computerIp")
  private String computerIp;

  /** Application working directory. */
  @JsonProperty("userDir")
  private String userDir;

  /** Operating system name. */
  @JsonProperty("osName")
  private String osName;

  /** System architecture (e.g., amd64, arm64). */
  @JsonProperty("osArch")
  private String osArch;

  /** Log current system info for debugging purposes. */
  public void logSystemInfo() {
    log.info(
        "System Information - Name: {}, IP: {}, OS: {} {}, UserDir: {}",
        computerName,
        computerIp,
        osName,
        osArch,
        userDir);
  }
}
