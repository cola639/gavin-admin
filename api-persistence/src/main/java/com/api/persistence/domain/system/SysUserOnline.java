package com.api.persistence.domain.system;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.Instant;

/**
 * Represents an active user session in the system.
 *
 * <p>This entity tracks users currently online, including their IP address, browser, operating
 * system, and login metadata.
 *
 * <p>Design principles: - Uses Lombok for reduced boilerplate. - Uses Jackson for JSON
 * serialization. - Uses JPA annotations for optional persistence support. - Logs changes for
 * debugging and monitoring. - Fully compatible with Java 17 and Spring Boot 3.5.
 */
@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SysUserOnline implements Serializable {

  private static final long serialVersionUID = 1L;

  /** Unique session identifier (JWT token ID). */
  private String tokenId;

  /** Department name of the user. */
  private String deptName;

  /** Username of the logged-in user. */
  private String userName;

  /** IP address of the logged-in user. */
  private String ipaddr;

  /** Login location derived from IP address. */
  private String loginLocation;

  /** Browser used during login (e.g., Chrome, Edge, Safari). */
  private String browser;

  /** Operating system of the client device. */
  private String os;

  /** Login timestamp (epoch milliseconds). */
  private Long loginTime;

  /** Logs session summary for diagnostics. */
  public void logSessionInfo() {
    log.info(
        "Online session [{}]: user='{}', ip='{}', browser='{}', os='{}'",
        tokenId,
        userName,
        ipaddr,
        browser,
        os);
  }
}
