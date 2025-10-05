package com.api.framework.domain;

import com.api.common.utils.Arith;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/** Represents system memory information. */
@Slf4j
@Data
public class Mem implements Serializable {

  private static final long serialVersionUID = 1L;

  /** Total memory (in bytes, internal use only). */
  @JsonIgnore private double total;

  /** Used memory (in bytes, internal use only). */
  @JsonIgnore private double used;

  /** Free memory (in bytes, internal use only). */
  @JsonIgnore private double free;

  /** Gets total memory in gigabytes (GB). */
  @JsonProperty("totalGB")
  public double getTotalGB() {
    double result = Arith.div(total, (1024 * 1024 * 1024), 2);
    log.debug("Calculated total memory: {} GB", result);
    return result;
  }

  /** Gets used memory in gigabytes (GB). */
  @JsonProperty("usedGB")
  public double getUsedGB() {
    double result = Arith.div(used, (1024 * 1024 * 1024), 2);
    log.debug("Calculated used memory: {} GB", result);
    return result;
  }

  /** Gets free memory in gigabytes (GB). */
  @JsonProperty("freeGB")
  public double getFreeGB() {
    double result = Arith.div(free, (1024 * 1024 * 1024), 2);
    log.debug("Calculated free memory: {} GB", result);
    return result;
  }

  /**
   * Gets memory usage as a percentage.
   *
   * @return Memory usage ratio (0–100)
   */
  @JsonProperty("usagePercent")
  public double getUsagePercent() {
    if (total == 0) {
      log.warn("Total memory is zero, returning 0 usage.");
      return 0D;
    }
    double result = Arith.mul(Arith.div(used, total, 4), 100);
    log.debug("Calculated memory usage: {}%", result);
    return result;
  }
}
