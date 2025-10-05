package com.api.framework.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * Represents real-time CPU usage information.
 *
 * <p>This class is a pure data object (not persisted to any database), used for transferring CPU
 * metrics between backend and frontend.
 *
 * <p>Design principles: - Uses Lombok to eliminate boilerplate code. - Uses Jackson for JSON
 * serialization. - Includes logging for safety and diagnostics. - Fully compatible with JDK 8.
 */
@Slf4j
@Data
public class Cpu implements Serializable {

  private static final long serialVersionUID = 1L;

  /** Number of CPU cores. */
  private Integer cpuNum;

  /** Total CPU usage (base value, not percentage). */
  @JsonIgnore private Double total;

  /** CPU system usage rate (base value, not percentage). */
  @JsonIgnore private Double sys;

  /** CPU user usage rate (base value, not percentage). */
  @JsonIgnore private Double used;

  /** CPU waiting rate (base value, not percentage). */
  @JsonIgnore private Double wait;

  /** CPU free rate (base value, not percentage). */
  @JsonIgnore private Double free;

  /** CPU total usage in percentage format. */
  @JsonProperty("totalUsage")
  public Double getTotalPercent() {
    return calculatePercent(total);
  }

  /** CPU system usage in percentage format. */
  @JsonProperty("systemUsage")
  public Double getSystemPercent() {
    return calculateRatio(sys);
  }

  /** CPU user usage in percentage format. */
  @JsonProperty("userUsage")
  public Double getUserPercent() {
    return calculateRatio(used);
  }

  /** CPU waiting rate in percentage format. */
  @JsonProperty("waitRate")
  public Double getWaitPercent() {
    return calculateRatio(wait);
  }

  /** CPU free rate in percentage format. */
  @JsonProperty("freeRate")
  public Double getFreePercent() {
    return calculateRatio(free);
  }

  /**
   * Calculates percentage based on total safely.
   *
   * @param value CPU metric value
   * @return Rounded percentage
   */
  private Double calculateRatio(Double value) {
    if (total == null || total == 0) {
      log.warn("Total CPU usage is zero or null, returning 0 for ratio.");
      return 0D;
    }
    return Math.round((value / total * 100D) * 100.0) / 100.0;
  }

  /**
   * Calculates total percentage.
   *
   * @param value base CPU metric
   * @return Rounded total percentage
   */
  private Double calculatePercent(Double value) {
    if (value == null) {
      log.warn("CPU total value is null, returning 0.");
      return 0D;
    }
    return Math.round(value * 10000.0) / 100.0;
  }
}
